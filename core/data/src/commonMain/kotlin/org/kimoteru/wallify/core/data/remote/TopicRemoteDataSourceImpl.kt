package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.topic.TopicsOrder
import org.kimoteru.wallify.core.network.api.TopicsApi
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.kimoteru.wallify.core.network.model.response.topic.toDomain
import org.koin.core.annotation.Singleton

@Singleton
class TopicRemoteDataSourceImpl(
    private val topicsApi: TopicsApi
) : TopicRemoteDataSource {

    override suspend fun getTopics(
        ids: List<String>,
        order: TopicsOrder?,
        page: Int,
        size: Int
    ): Response<List<Topic>> {
        return topicsApi.getTopics(page = page, perPage = size, ids = ids, order = order)
            .map { topics -> topics.map { topic -> topic.toDomain() } }
    }

    override suspend fun getTopic(idOrSlug: String): Response<Topic> {
        return topicsApi.getTopic(idOrSlug = idOrSlug)
            .map { it.toDomain() }
    }

    override suspend fun getTopicPhotos(
        idOrSlug: String,
        orientation: PhotoOrientation?,
        order: PhotosOrder?,
        page: Int,
        size: Int
    ): Response<List<Photo>> {
        return topicsApi.getTopicPhotos(
            idOrSlug = idOrSlug,
            page = page,
            perPage = size,
            orientation = orientation,
            order = order
        ).map { photos -> photos.map { photo -> photo.toDomain() } }
    }
}