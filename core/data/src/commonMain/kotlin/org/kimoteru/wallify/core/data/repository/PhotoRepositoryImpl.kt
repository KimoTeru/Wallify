package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.onSuccess
import org.kimoteru.wallify.core.data.cache.CacheLoader
import org.kimoteru.wallify.core.data.cache.CacheStore
import org.kimoteru.wallify.core.data.cache.CacheTtl
import org.kimoteru.wallify.core.data.cache.ItemKeys
import org.kimoteru.wallify.core.data.cache.ListKeys
import org.kimoteru.wallify.core.data.cache.StatisticsKeys
import org.kimoteru.wallify.core.data.cache.canUseStaleData
import org.kimoteru.wallify.core.data.cache.pager
import org.kimoteru.wallify.core.data.cache.refreshableFlow
import org.kimoteru.wallify.core.data.remote.PhotoRemoteDataSource
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.entity.toDomain
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.UpdatePhotoParams
import org.kimoteru.wallify.core.model.stats.Statistics
import org.koin.core.annotation.Singleton

@Singleton
class PhotoRepositoryImpl(
    private val photoRemoteDataSource: PhotoRemoteDataSource,
    private val photoDao: PhotoDao,
    private val statsDao: StatsDao,
    private val cacheLoader: CacheLoader,
    private val cacheStore: CacheStore,
    private val dispatchers: AppDispatchers
) : PhotoRepository {

    override fun getPhotosFlow(): Flow<PagingData<Photo>> {
        return cacheLoader.pager(
            dispatchers = dispatchers,
            listKey = ListKeys.PHOTOS_FEED,
            ttl = CacheTtl.Feed,
            pagingSource = { photoDao.pagingSource(ListKeys.PHOTOS_FEED) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { photo -> photo.id },
            fetch = { page, size -> photoRemoteDataSource.getPhotos(page = page, size = size) },
            saveItems = cacheStore::savePhotoSummaries
        )
    }

    override fun getPhotoFlow(id: String): Flow<Resource<Photo>> {
        return refreshableFlow(
            loader = cacheLoader,
            dispatchers = dispatchers,
            key = ItemKeys.photo(id),
            ttl = CacheTtl.Item,
            roomFlow = { photoDao.observe(id) },
            remoteCall = { photoRemoteDataSource.getPhoto(id = id) },
            save = cacheStore::savePhoto,
            toDomain = { entity -> entity.toDomain() }
        )
    }

    /** Every call asks the backend for a new photo; the answer is saved with the other photos. */
    override suspend fun getRandomPhoto(query: RandomPhotoQuery): Response<Photo> =
        withContext(dispatchers.io) {
            photoRemoteDataSource.getRandomPhoto(query = query)
                .onSuccess { photo -> cacheLoader.saveQuietly { cacheStore.savePhotoSummaries(listOf(photo)) } }
        }

    /**
     * With [forceRefresh] every call asks the backend for a new set and saves it. When the backend cannot answer (no
     * connection, the request limit), the last saved set for the same request is shown instead of an error. Without
     * it a set saved less than [CacheTtl.Feed] ago is returned as it is.
     */
    override suspend fun getRandomPhotos(
        count: Int,
        query: RandomPhotoQuery,
        forceRefresh: Boolean
    ): Response<List<Photo>> = withContext(dispatchers.io) {
        val listKey = ListKeys.randomPhotos(count, query)

        if (!forceRefresh && cacheLoader.isFresh(listKey, CacheTtl.Feed)) {
            val saved = photoDao.getList(listKey, count)
            if (saved.isNotEmpty()) return@withContext Response.Success(saved.map { entity -> entity.toDomain() })
        }

        when (val response = photoRemoteDataSource.getRandomPhotos(count = count, query = query)) {
            is Response.Success -> {
                cacheLoader.saveQuietly { cacheStore.savePhotoList(listKey, response.data) }
                response
            }
            is Response.Error -> {
                val saved = if (response.errorData.canUseStaleData()) photoDao.getList(listKey, count) else emptyList()
                if (saved.isEmpty()) response else Response.Success(saved.map { entity -> entity.toDomain() })
            }
        }
    }

    override fun getPhotoStatisticsFlow(id: String, quantity: Int?): Flow<Resource<Statistics>> {
        val key = StatisticsKeys.photo(id, quantity)
        return refreshableFlow(
            loader = cacheLoader,
            dispatchers = dispatchers,
            key = ItemKeys.photoStatistics(id, quantity),
            ttl = CacheTtl.Stats,
            roomFlow = { statsDao.observeStatistics(key) },
            remoteCall = { photoRemoteDataSource.getPhotoStatistics(id = id, quantity = quantity) },
            save = { statistics -> cacheStore.saveStatistics(key, statistics) },
            toDomain = { entity -> entity.toDomain() }
        )
    }

    /** A download is an action: it is reported every time and never answered from the database. */
    override suspend fun trackDownload(photoId: String, quality: ImageQuality): Response<String> =
        withContext(dispatchers.io) {
            photoRemoteDataSource.trackDownload(id = photoId, quality = quality)
        }

    override suspend fun updatePhoto(id: String, params: UpdatePhotoParams): Response<Photo> =
        withContext(dispatchers.io) {
            photoRemoteDataSource.updatePhoto(id = id, params = params)
                .onSuccess { photo -> cacheLoader.saveQuietly { cacheStore.savePhoto(photo) } }
        }
}