package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.data.cache.CacheLoader
import org.kimoteru.wallify.core.data.cache.CacheStore
import org.kimoteru.wallify.core.data.cache.CacheTtl
import org.kimoteru.wallify.core.data.cache.ListKeys
import org.kimoteru.wallify.core.data.cache.pager
import org.kimoteru.wallify.core.data.cache.refreshableFlow
import org.kimoteru.wallify.core.data.remote.TopicRemoteDataSource
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.TopicDao
import org.kimoteru.wallify.core.database.entity.toDomain
import org.kimoteru.wallify.core.domain.repository.TopicRepository
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.topic.TopicsOrder
import org.koin.core.annotation.Singleton

@Singleton
class TopicRepositoryImpl(
    private val topicRemoteDataSource: TopicRemoteDataSource,
    private val topicDao: TopicDao,
    private val photoDao: PhotoDao,
    private val cacheLoader: CacheLoader,
    private val cacheStore: CacheStore
) : TopicRepository {

    override fun getTopicsFlow(ids: List<String>, order: TopicsOrder?): Flow<PagingData<Topic>> {
        val listKey = ListKeys.topics(ids, order)
        return cacheLoader.pager(
            listKey = listKey,
            ttl = CacheTtl.Slow,
            pagingSource = { topicDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { topic -> topic.id },
            fetch = { page, size ->
                topicRemoteDataSource.getTopics(ids = ids, order = order, page = page, size = size)
            },
            saveItems = cacheStore::saveTopics
        )
    }

    override fun getTopicFlow(idOrSlug: String): Flow<Resource<Topic>> {
        return refreshableFlow(
            roomFlow = { topicDao.observeByIdOrSlug(idOrSlug) },
            remoteCall = { topicRemoteDataSource.getTopic(idOrSlug = idOrSlug) },
            save = cacheStore::saveTopic,
            toDomain = { entity -> entity.toDomain() }
        )
    }

    override fun getTopicPhotosFlow(
        idOrSlug: String,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Flow<PagingData<Photo>> {
        val listKey = ListKeys.topicPhotos(idOrSlug, orientation, order)
        return cacheLoader.pager(
            listKey = listKey,
            ttl = CacheTtl.Feed,
            pagingSource = { photoDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { photo -> photo.id },
            fetch = { page, size ->
                topicRemoteDataSource.getTopicPhotos(
                    idOrSlug = idOrSlug,
                    orientation = orientation,
                    order = order,
                    page = page,
                    size = size
                )
            },
            saveItems = cacheStore::savePhotoSummaries
        )
    }
}