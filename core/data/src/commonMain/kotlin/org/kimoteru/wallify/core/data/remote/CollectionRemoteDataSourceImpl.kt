package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.network.api.CollectionsApi
import org.kimoteru.wallify.core.network.model.request.collection.CollectionRequest
import org.kimoteru.wallify.core.network.model.response.collection.toDomain
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.koin.core.annotation.Singleton

@Singleton
class CollectionRemoteDataSourceImpl(
    private val collectionsApi: CollectionsApi
) : CollectionRemoteDataSource {

    override suspend fun getCollections(page: Int, size: Int): Response<List<PhotoCollection>> {
        return collectionsApi.getCollections(page = page, perPage = size)
            .map { collections -> collections.map { collection -> collection.toDomain() } }
    }

    override suspend fun getCollection(id: Int): Response<PhotoCollection> {
        return collectionsApi.getCollection(id = id)
            .map { it.toDomain() }
    }

    override suspend fun getCollectionPhotos(
        id: Int,
        page: Int,
        size: Int,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Response<List<Photo>> {
        return collectionsApi.getCollectionPhotos(
            id = id,
            page = page,
            perPage = size,
            orientation = orientation,
            order = order
        ).map { photos -> photos.map { photo -> photo.toDomain() } }
    }

    override suspend fun getRelatedCollections(id: Int): Response<List<PhotoCollection>> {
        return collectionsApi.getRelatedCollections(id = id)
            .map { collections -> collections.map { collection -> collection.toDomain() } }
    }

    override suspend fun createCollection(
        title: String,
        description: String?,
        isPrivate: Boolean?
    ): Response<PhotoCollection> {
        return collectionsApi.createCollection(
            request = CollectionRequest(title = title, description = description, isPrivate = isPrivate)
        ).map { it.toDomain() }
    }

    override suspend fun updateCollection(
        id: Int,
        title: String?,
        description: String?,
        isPrivate: Boolean?
    ): Response<PhotoCollection> {
        return collectionsApi.updateCollection(
            id = id,
            request = CollectionRequest(title = title, description = description, isPrivate = isPrivate)
        ).map { it.toDomain() }
    }

    override suspend fun deleteCollection(id: Int): Response<Unit> {
        return collectionsApi.deleteCollection(id = id)
    }

    override suspend fun addPhotoToCollection(collectionId: Int, photoId: String): Response<CollectionPhotoChange> {
        return collectionsApi.addPhoto(collectionId = collectionId, photoId = photoId)
            .map { it.toDomain() }
    }

    override suspend fun removePhotoFromCollection(
        collectionId: Int,
        photoId: String
    ): Response<CollectionPhotoChange> {
        return collectionsApi.removePhoto(collectionId = collectionId, photoId = photoId)
            .map { it.toDomain() }
    }
}