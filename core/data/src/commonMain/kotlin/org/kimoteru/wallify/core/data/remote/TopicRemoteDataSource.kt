package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.topic.TopicsOrder

interface TopicRemoteDataSource {

    suspend fun getTopics(ids: List<String>, order: TopicsOrder?, page: Int, size: Int): Response<List<Topic>>

    suspend fun getTopic(idOrSlug: String): Response<Topic>

    suspend fun getTopicPhotos(
        idOrSlug: String,
        orientation: PhotoOrientation?,
        order: PhotosOrder?,
        page: Int,
        size: Int
    ): Response<List<Photo>>
}