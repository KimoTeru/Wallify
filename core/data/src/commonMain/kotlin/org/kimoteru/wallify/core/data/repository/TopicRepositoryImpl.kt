package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.paging.createPager
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.data.remote.TopicRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.TopicRepository
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.topic.TopicsOrder
import org.koin.core.annotation.Singleton

@Singleton
class TopicRepositoryImpl(
    private val topicRemoteDataSource: TopicRemoteDataSource
) : TopicRepository {

    override fun getTopicsFlow(ids: List<String>, order: TopicsOrder?): Flow<PagingData<Topic>> {
        return createPager { page, loadSize ->
            topicRemoteDataSource.getTopics(ids = ids, order = order, page = page, size = loadSize)
        }
    }

    override suspend fun getTopic(idOrSlug: String): Response<Topic> {
        return topicRemoteDataSource.getTopic(idOrSlug = idOrSlug)
    }

    override fun getTopicPhotosFlow(
        idOrSlug: String,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Flow<PagingData<Photo>> {
        return createPager { page, loadSize ->
            topicRemoteDataSource.getTopicPhotos(
                idOrSlug = idOrSlug,
                orientation = orientation,
                order = order,
                page = page,
                size = loadSize
            )
        }
    }
}