package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.paging.DEFAULT_PAGE_SIZE
import org.kimoteru.wallify.core.common.paging.FIRST_PAGE
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
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
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.kimoteru.wallify.core.database.entity.toDomain
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User
import org.koin.core.annotation.Singleton

@Singleton
class UserRepositoryImpl(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userDao: UserDao,
    private val photoDao: PhotoDao,
    private val collectionDao: CollectionDao,
    private val statsDao: StatsDao,
    private val cacheLoader: CacheLoader,
    private val cacheStore: CacheStore,
    private val scope: CoroutineScope
) : UserRepository {

    /** The signed-in user comes from the database, so the app opens with the user it saw last, even offline. */
    override val currentUser: StateFlow<User?> = userDao.observeCurrent()
        .map { entity -> entity?.toDomain() }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = null)

    /** Writes the answer into the database; [currentUser] follows from there. */
    override suspend fun refreshCurrentUser(): Response<User> {
        return userRemoteDataSource.getCurrentUser()
            .onSuccess { user -> cacheLoader.saveQuietly { cacheStore.saveCurrentUser(user) } }
    }

    override fun clearCurrentUser() {
        scope.launch { userDao.deleteCurrent() }
    }

    override suspend fun updateCurrentUser(params: UpdateUserParams): Response<User> {
        return userRemoteDataSource.updateCurrentUser(params = params)
            .onSuccess { user -> cacheLoader.saveQuietly { cacheStore.saveCurrentUser(user) } }
    }

    override val uploadPageUrl: String = userRemoteDataSource.uploadPageUrl

    override fun getUserFlow(username: String): Flow<Resource<User>> {
        return refreshableFlow(
            loader = cacheLoader,
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

    override suspend fun invalidateUserPhotos(username: String) {
        cacheStore.invalidateLists(ListKeys.userPhotosPrefix(username))
        cacheStore.invalidateLists(ItemKeys.userPrefix(username))
    }

    override fun getUserCollectionsFlow(username: String): Flow<PagingData<PhotoCollection>> {
        val listKey = ListKeys.userCollections(username)

        return cacheLoader.pager(
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
            key = ItemKeys.userStatistics(username, quantity),
            ttl = CacheTtl.Stats,
            roomFlow = { statsDao.observeStatistics(key) },
            remoteCall = { userRemoteDataSource.getUserStatistics(username = username, quantity = quantity) },
            save = { statistics -> cacheStore.saveStatistics(key, statistics) },
            toDomain = { entity -> entity.toDomain() }
        )
    }
}