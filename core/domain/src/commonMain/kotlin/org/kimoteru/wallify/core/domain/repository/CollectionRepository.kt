package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder

/** Create, update, delete, add and remove need the user token with the collections write scope. */
interface CollectionRepository {

    fun getCollectionsFlow(): Flow<PagingData<PhotoCollection>>

    suspend fun getCollection(id: Int): Response<PhotoCollection>

    fun getCollectionPhotosFlow(
        id: Int,
        orientation: PhotoOrientation? = null,
        order: PhotosOrder? = null
    ): Flow<PagingData<Photo>>

    suspend fun getRelatedCollections(id: Int): Response<List<PhotoCollection>>

    suspend fun createCollection(
        title: String,
        description: String? = null,
        isPrivate: Boolean? = null
    ): Response<PhotoCollection>

    /** `null` arguments leave the field as it is. */
    suspend fun updateCollection(
        id: Int,
        title: String? = null,
        description: String? = null,
        isPrivate: Boolean? = null
    ): Response<PhotoCollection>

    suspend fun deleteCollection(id: Int): Response<Unit>

    /** Adding a photo that is already in the collection has no effect. */
    suspend fun addPhotoToCollection(collectionId: Int, photoId: String): Response<CollectionPhotoChange>

    suspend fun removePhotoFromCollection(collectionId: Int, photoId: String): Response<CollectionPhotoChange>
}