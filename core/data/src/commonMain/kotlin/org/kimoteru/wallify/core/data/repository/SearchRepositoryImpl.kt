package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.data.cache.CacheLoader
import org.kimoteru.wallify.core.data.cache.CacheStore
import org.kimoteru.wallify.core.data.cache.CacheTtl
import org.kimoteru.wallify.core.data.cache.ListKeys
import org.kimoteru.wallify.core.data.cache.pager
import org.kimoteru.wallify.core.data.remote.SearchRemoteDataSource
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.kimoteru.wallify.core.database.entity.toDomain
import org.kimoteru.wallify.core.domain.repository.SearchRepository
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.model.user.User
import org.koin.core.annotation.Singleton

@Singleton
class SearchRepositoryImpl(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val photoDao: PhotoDao,
    private val collectionDao: CollectionDao,
    private val userDao: UserDao,
    private val cacheLoader: CacheLoader,
    private val cacheStore: CacheStore
) : SearchRepository {

    override fun searchPhotosFlow(query: SearchPhotosQuery): Flow<PagingData<Photo>> {
        val listKey = ListKeys.searchPhotos(query)
        return cacheLoader.pager(
            listKey = listKey,
            ttl = CacheTtl.Feed,
            pagingSource = { photoDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { photo -> photo.id },
            fetch = { page, size -> searchRemoteDataSource.searchPhotos(query = query, page = page, size = size) },
            saveItems = cacheStore::savePhotoSummaries
        )
    }

    override fun searchCollectionsFlow(query: String): Flow<PagingData<PhotoCollection>> {
        val listKey = ListKeys.searchCollections(query)
        return cacheLoader.pager(
            listKey = listKey,
            ttl = CacheTtl.Feed,
            pagingSource = { collectionDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { collection -> collection.id.toString() },
            fetch = { page, size ->
                searchRemoteDataSource.searchCollections(query = query, page = page, size = size)
            },
            saveItems = cacheStore::saveCollections
        )
    }

    override fun searchUsersFlow(query: String): Flow<PagingData<User>> {
        val listKey = ListKeys.searchUsers(query)
        return cacheLoader.pager(
            listKey = listKey,
            ttl = CacheTtl.Feed,
            pagingSource = { userDao.pagingSource(listKey) },
            toDomain = { entity -> entity.toDomain() },
            itemId = { user -> user.id },
            fetch = { page, size -> searchRemoteDataSource.searchUsers(query = query, page = page, size = size) },
            saveItems = cacheStore::saveUserSummaries
        )
    }
}