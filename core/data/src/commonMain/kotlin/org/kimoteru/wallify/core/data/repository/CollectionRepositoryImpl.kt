package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.paging.DEFAULT_PAGE_SIZE
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.onSuccess
import org.kimoteru.wallify.core.data.cache.CacheLoader
import org.kimoteru.wallify.core.data.cache.CacheStore
import org.kimoteru.wallify.core.data.cache.CacheTtl
import org.kimoteru.wallify.core.data.cache.ItemKeys
import org.kimoteru.wallify.core.data.cache.ListKeys
import org.kimoteru.wallify.core.data.cache.pager
import org.kimoteru.wallify.core.data.cache.refreshableFlow
import org.kimoteru.wallify.core.data.cache.refreshableListFlow
import org.kimoteru.wallify.core.data.remote.CollectionRemoteDataSource
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.entity.toDomain
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.koin.core.annotation.Singleton

@Singleton
class CollectionRepositoryImpl(
    private val collectionRemoteDataSource: CollectionRemoteDataSource,
    private val collectionDao: CollectionDao,
    private val photoDao: PhotoDao,
    private val cacheLoader: CacheLoader,
    private val cacheStore: CacheStore,
    private val dispatchers: AppDispatchers
) : CollectionRepository {

    override fun getCollectionsFlow(): Flow<PagingData<PhotoCollection>> {
        return cacheLoader.pager(
            dispatchers = dispatchers,
            listKey = ListKeys.COLLECTIONS_FEED,
            ttl = CacheTtl.Feed,
            pagingSource = { collectionDao.pagingSource(ListKeys.COLLECTIONS_FEED) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { collection -> collection.id.toString() },
            fetch = { page, size -> collectionRemoteDataSource.getCollections(page = page, size = size) },
            saveItems = cacheStore::saveCollections
        )
    }

    override fun getCollectionFlow(id: Int): Flow<Resource<PhotoCollection>> {
        return refreshableFlow(
            loader = cacheLoader,
            dispatchers = dispatchers,
            key = ItemKeys.collection(id),
            ttl = CacheTtl.Item,
            roomFlow = { collectionDao.observe(id) },
            remoteCall = { collectionRemoteDataSource.getCollection(id = id) },
            save = cacheStore::saveCollection,
            toDomain = { entity -> entity.toDomain() }
        )
    }

    override fun getCollectionPhotosFlow(
        id: Int,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Flow<PagingData<Photo>> {
        val listKey = ListKeys.collectionPhotos(id, orientation, order)
        return cacheLoader.pager(
            dispatchers = dispatchers,
            listKey = listKey,
            ttl = CacheTtl.Feed,
            pagingSource = { photoDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { photo -> photo.id },
            fetch = { page, size ->
                collectionRemoteDataSource.getCollectionPhotos(
                    id = id,
                    page = page,
                    size = size,
                    orientation = orientation,
                    order = order
                )
            },
            saveItems = cacheStore::savePhotoSummaries
        )
    }

    override fun getRelatedCollectionsFlow(id: Int): Flow<Resource<List<PhotoCollection>>> {
        val listKey = ListKeys.relatedCollections(id)
        return refreshableListFlow(
            loader = cacheLoader,
            dispatchers = dispatchers,
            key = listKey,
            ttl = CacheTtl.Feed,
            roomFlow = { collectionDao.observeList(listKey = listKey, limit = DEFAULT_PAGE_SIZE) },
            remoteCall = { collectionRemoteDataSource.getRelatedCollections(id = id) },
            save = { collections -> cacheStore.saveCollectionList(listKey, collections) },
            toDomain = { entity -> entity.toDomain() }
        )
    }

    override suspend fun createCollection(
        title: String,
        description: String?,
        isPrivate: Boolean?
    ): Response<PhotoCollection> = withContext(dispatchers.io) {
        collectionRemoteDataSource.createCollection(
            title = title,
            description = description,
            isPrivate = isPrivate
        ).onSuccess { collection -> saveChanged(collection) }
    }

    override suspend fun updateCollection(
        id: Int,
        title: String?,
        description: String?,
        isPrivate: Boolean?
    ): Response<PhotoCollection> = withContext(dispatchers.io) {
        collectionRemoteDataSource.updateCollection(
            id = id,
            title = title,
            description = description,
            isPrivate = isPrivate
        ).onSuccess { collection -> saveChanged(collection) }
    }

    override suspend fun deleteCollection(id: Int): Response<Unit> = withContext(dispatchers.io) {
        collectionRemoteDataSource.deleteCollection(id = id)
            .onSuccess {
                cacheLoader.saveQuietly {
                    collectionDao.delete(id)
                    cacheStore.invalidateLists(ListKeys.COLLECTIONS_PREFIX)
                }
            }
    }

    override suspend fun addPhotoToCollection(
        collectionId: Int,
        photoId: String
    ): Response<CollectionPhotoChange> = withContext(dispatchers.io) {
        collectionRemoteDataSource.addPhotoToCollection(collectionId = collectionId, photoId = photoId)
            .onSuccess { change -> saveChangedMembership(change, photoId, isMember = true) }
    }

    override suspend fun removePhotoFromCollection(
        collectionId: Int,
        photoId: String
    ): Response<CollectionPhotoChange> = withContext(dispatchers.io) {
        collectionRemoteDataSource.removePhotoFromCollection(collectionId = collectionId, photoId = photoId)
            .onSuccess { change -> saveChangedMembership(change, photoId, isMember = false) }
    }

    /**
     * The collection changed on the backend: its row is replaced by the answer, so every screen showing it updates,
     * and every list of collections is loaded again the next time it opens (titles and counters changed), while the
     * saved lists stay on screen.
     */
    private suspend fun saveChanged(collection: PhotoCollection) {
        cacheLoader.saveQuietly {
            cacheStore.saveCollection(collection)
            cacheStore.invalidateLists(ListKeys.COLLECTIONS_PREFIX)
        }
    }

    /** Besides the collection, the photo itself changed: it carries the ids of the user's collections that hold it. */
    private suspend fun saveChangedMembership(change: CollectionPhotoChange, photoId: String, isMember: Boolean) {
        saveChanged(change.collection)
        cacheLoader.saveQuietly {
            cacheStore.changePhotoMembership(
                photoId = photoId,
                collectionId = change.collection.id,
                isMember = isMember
            )
        }
    }
}