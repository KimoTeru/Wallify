package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.topic.TopicsOrder

interface TopicRepository {

    /** [ids] limits the list to topics with these ids or slugs. */
    fun getTopicsFlow(ids: List<String> = emptyList(), order: TopicsOrder? = null): Flow<PagingData<Topic>>

    /** Behaves like `PhotoRepository.getPhotoFlow`: the database first, refreshed from the backend. */
    fun getTopicFlow(idOrSlug: String): Flow<Resource<Topic>>

    fun getTopicPhotosFlow(
        idOrSlug: String,
        orientation: PhotoOrientation? = null,
        order: PhotosOrder? = null
    ): Flow<PagingData<Photo>>
}