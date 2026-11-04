package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.paging.createPager
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.data.remote.CollectionRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.koin.core.annotation.Singleton

@Singleton
class CollectionRepositoryImpl(
    private val collectionRemoteDataSource: CollectionRemoteDataSource
) : CollectionRepository {

    override fun getCollectionsFlow(): Flow<PagingData<PhotoCollection>> {
        return createPager { page, loadSize ->
            collectionRemoteDataSource.getCollections(page = page, size = loadSize)
        }
    }

    override suspend fun getCollection(id: Int): Response<PhotoCollection> {
        return collectionRemoteDataSource.getCollection(id = id)
    }

    override fun getCollectionPhotosFlow(
        id: Int,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Flow<PagingData<Photo>> {
        return createPager { page, loadSize ->
            collectionRemoteDataSource.getCollectionPhotos(
                id = id,
                page = page,
                size = loadSize,
                orientation = orientation,
                order = order
            )
        }
    }

    override suspend fun getRelatedCollections(id: Int): Response<List<PhotoCollection>> {
        return collectionRemoteDataSource.getRelatedCollections(id = id)
    }

    override suspend fun createCollection(
        title: String,
        description: String?,
        isPrivate: Boolean?
    ): Response<PhotoCollection> {
        return collectionRemoteDataSource.createCollection(
            title = title,
            description = description,
            isPrivate = isPrivate
        )
    }

    override suspend fun updateCollection(
        id: Int,
        title: String?,
        description: String?,
        isPrivate: Boolean?
    ): Response<PhotoCollection> {
        return collectionRemoteDataSource.updateCollection(
            id = id,
            title = title,
            description = description,
            isPrivate = isPrivate
        )
    }

    override suspend fun deleteCollection(id: Int): Response<Unit> {
        return collectionRemoteDataSource.deleteCollection(id = id)
    }

    override suspend fun addPhotoToCollection(collectionId: Int, photoId: String): Response<CollectionPhotoChange> {
        return collectionRemoteDataSource.addPhotoToCollection(collectionId = collectionId, photoId = photoId)
    }

    override suspend fun removePhotoFromCollection(
        collectionId: Int,
        photoId: String
    ): Response<CollectionPhotoChange> {
        return collectionRemoteDataSource.removePhotoFromCollection(collectionId = collectionId, photoId = photoId)
    }
}