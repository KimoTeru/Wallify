package org.kimoteru.wallify.core.data.repository

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.paging.DEFAULT_PAGE_SIZE
import org.kimoteru.wallify.core.common.paging.FIRST_PAGE
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.onSuccess
import org.kimoteru.wallify.core.data.cache.CacheLoader
import org.kimoteru.wallify.core.data.cache.CacheStore
import org.kimoteru.wallify.core.data.cache.CacheTtl
import org.kimoteru.wallify.core.data.cache.ItemKeys
import org.kimoteru.wallify.core.data.cache.ListKeys
import org.kimoteru.wallify.core.data.cache.StatisticsKeys
import org.kimoteru.wallify.core.data.cache.pager
import org.kimoteru.wallify.core.data.cache.refreshableFlow
import org.kimoteru.wallify.core.data.cache.refreshableListFlow
import org.kimoteru.wallify.core.data.remote.UserRemoteDataSource
import org.kimoteru.wallify.core.data.session.CurrentUserSync
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.kimoteru.wallify.core.database.entity.toDomain
import org.kimoteru.wallify.core.datastore.prefs.DataStorePrefs
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository
import org.koin.core.annotation.Singleton

/**
 * Created with Koin: the repository itself keeps [currentUser] in step with the session ([CurrentUserSync]), so it
 * runs from the start of the app, before any screen asks.
 */
@Singleton(createdAtStart = true)
class UserRepositoryImpl(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userDao: UserDao,
    private val photoDao: PhotoDao,
    private val collectionDao: CollectionDao,
    private val statsDao: StatsDao,
    private val cacheLoader: CacheLoader,
    private val cacheStore: CacheStore,
    private val prefs: DataStorePrefs,
    private val dispatchers: AppDispatchers,
    private val scope: CoroutineScope,
    headersCollector: HeadersCollectorRepository,
    session: SessionRepository,
    connectivityRepository: ConnectivityRepository
) : UserRepository {

    /** The signed-in user comes from the database, so the app opens with the user it saw last, even offline. */
    override val currentUser: StateFlow<User?> = userDao.observeCurrent()
        .map { entity -> entity?.toDomain() }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = null)

    private val currentUserSync = CurrentUserSync(
        headersCollector = headersCollector,
        session = session,
        connectivityRepository = connectivityRepository,
        refresh = ::refreshCurrentUser,
        clear = ::clearCurrentUser
    ).also { it.start(scope) }

    override val currentUserLoadError: StateFlow<ResponseError?> = currentUserSync.loadError

    override fun retryCurrentUserLoad() = currentUserSync.retry()

    /**
     * Writes the answer into the database; [currentUser] follows from there. A saved user loaded less than
     * [CacheTtl.Item] ago for the same [authorization] is not asked again, so a start of the app costs no request.
     * A user saved for another session (a sign-in, maybe into another account) is always loaded anew. The answer is
     * the full profile, so it also counts as the load of [getUserFlow]: the own profile does not ask for the same
     * user once more.
     */
    private suspend fun refreshCurrentUser(authorization: String): Response<User> = withContext(dispatchers.io) {
        val session = authorization.sessionId()
        val freshUser = userDao.getCurrent()
            ?.takeIf { prefs.getNullable(currentUserSessionKey) == session }
            ?.takeIf { saved -> cacheLoader.isFresh(ItemKeys.user(saved.username), CacheTtl.Item) }

        if (freshUser != null) Response.Success(freshUser.toDomain()) else loadCurrentUser(session)
    }

    private suspend fun loadCurrentUser(session: Int): Response<User> = userRemoteDataSource.getCurrentUser()
        .onSuccess { user ->
            cacheLoader.saveQuietly { cacheStore.saveCurrentUser(user) }
            cacheLoader.markLoaded(ItemKeys.user(user.username))
            cacheLoader.saveQuietly { prefs.save(currentUserSessionKey, session) }
        }

    override fun clearCurrentUser() {
        scope.launch {
            userDao.deleteCurrent()
            prefs.remove(currentUserSessionKey)
        }
    }

    /** Only tells sessions apart, so a hash and not the token itself: the token is not kept twice. */
    private fun String.sessionId(): Int = hashCode()

    override suspend fun updateCurrentUser(params: UpdateUserParams): Response<User> =
        withContext(dispatchers.io) {
            userRemoteDataSource.updateCurrentUser(params = params)
                .onSuccess { user -> cacheLoader.saveQuietly { cacheStore.saveCurrentUser(user) } }
        }

    override val uploadPageUrl: String = userRemoteDataSource.uploadPageUrl

    override fun getUserFlow(username: String): Flow<Resource<User>> {
        return refreshableFlow(
            loader = cacheLoader,
            dispatchers = dispatchers,
            key = ItemKeys.user(username),
            ttl = CacheTtl.Item,
            roomFlow = { userDao.observeByUsername(username) },
            remoteCall = { userRemoteDataSource.getUser(username = username) },
            save = cacheStore::saveUser,
            toDomain = { entity -> entity.toDomain() }
        )
    }

    override fun getUserPhotosFlow(
        username: String,
        order: UserPhotosOrder?,
        orientation: PhotoOrientation?
    ): Flow<PagingData<Photo>> {
        val listKey = ListKeys.userPhotos(username, order, orientation)

        return cacheLoader.pager(
            dispatchers = dispatchers,
            listKey = listKey,
            ttl = CacheTtl.Feed,
            pagingSource = { photoDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { photo -> photo.id },
            fetch = { page, size ->
                userRemoteDataSource.getUserPhotos(
                    username = username,
                    page = page,
                    size = size,
                    order = order,
                    orientation = orientation
                )
            },
            saveItems = cacheStore::savePhotoSummaries
        )
    }

    override suspend fun invalidateUserPhotos(username: String): Unit = withContext(dispatchers.io) {
        cacheStore.invalidateLists(ListKeys.userPhotosPrefix(username))
        cacheStore.invalidateLists(ItemKeys.userPrefix(username))
    }

    override fun getUserCollectionsFlow(username: String): Flow<PagingData<PhotoCollection>> {
        val listKey = ListKeys.userCollections(username)

        return cacheLoader.pager(
            dispatchers = dispatchers,
            listKey = listKey,
            ttl = CacheTtl.Feed,
            pagingSource = { collectionDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { collection -> collection.id.toString() },
            fetch = { page, size ->
                userRemoteDataSource.getUserCollections(username = username, page = page, size = size)
            },
            saveItems = cacheStore::saveCollections
        )
    }

    override fun getUserCollectionListFlow(username: String): Flow<Resource<List<PhotoCollection>>> {
        val listKey = ListKeys.userCollections(username)

        return refreshableListFlow(
            loader = cacheLoader,
            dispatchers = dispatchers,
            key = listKey,
            ttl = CacheTtl.Feed,
            roomFlow = { collectionDao.observeList(listKey = listKey, limit = DEFAULT_PAGE_SIZE) },
            remoteCall = {
                userRemoteDataSource.getUserCollections(
                    username = username,
                    page = FIRST_PAGE,
                    size = DEFAULT_PAGE_SIZE
                )
            },
            save = { collections -> cacheStore.saveCollectionList(listKey, collections) },
            toDomain = { entity -> entity.toDomain() }
        )
    }

    override fun getUserStatisticsFlow(username: String, quantity: Int?): Flow<Resource<Statistics>> {
        val key = StatisticsKeys.user(username, quantity)

        return refreshableFlow(
            loader = cacheLoader,
            dispatchers = dispatchers,
            key = ItemKeys.userStatistics(username, quantity),
            ttl = CacheTtl.Stats,
            roomFlow = { statsDao.observeStatistics(key) },
            remoteCall = { userRemoteDataSource.getUserStatistics(username = username, quantity = quantity) },
            save = { statistics -> cacheStore.saveStatistics(key, statistics) },
            toDomain = { entity -> entity.toDomain() }
        )
    }

    private companion object {
        /** The session the saved current user was loaded for. */
        val currentUserSessionKey = intPreferencesKey("current_user_session")
    }
}