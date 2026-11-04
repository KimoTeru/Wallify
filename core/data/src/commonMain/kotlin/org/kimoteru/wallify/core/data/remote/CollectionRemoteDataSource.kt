package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder

interface CollectionRemoteDataSource {

    suspend fun getCollections(page: Int, size: Int): Response<List<PhotoCollection>>

    suspend fun getCollection(id: Int): Response<PhotoCollection>

    suspend fun getCollectionPhotos(
        id: Int,
        page: Int,
        size: Int,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Response<List<Photo>>

    suspend fun getRelatedCollections(id: Int): Response<List<PhotoCollection>>

    suspend fun createCollection(title: String, description: String?, isPrivate: Boolean?): Response<PhotoCollection>

    suspend fun updateCollection(
        id: Int,
        title: String?,
        description: String?,
        isPrivate: Boolean?
    ): Response<PhotoCollection>

    suspend fun deleteCollection(id: Int): Response<Unit>

    suspend fun addPhotoToCollection(collectionId: Int, photoId: String): Response<CollectionPhotoChange>

    suspend fun removePhotoFromCollection(collectionId: Int, photoId: String): Response<CollectionPhotoChange>
}