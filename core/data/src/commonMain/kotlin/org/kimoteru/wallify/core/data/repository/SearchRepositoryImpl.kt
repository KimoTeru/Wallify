package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.paging.createPager
import org.kimoteru.wallify.core.data.remote.SearchRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.SearchRepository
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.model.user.User
import org.koin.core.annotation.Singleton

@Singleton
class SearchRepositoryImpl(
    private val searchRemoteDataSource: SearchRemoteDataSource
) : SearchRepository {

    override fun searchPhotosFlow(query: SearchPhotosQuery): Flow<PagingData<Photo>> {
        return createPager { page, loadSize ->
            searchRemoteDataSource.searchPhotos(query = query, page = page, size = loadSize)
        }
    }

    override fun searchCollectionsFlow(query: String): Flow<PagingData<PhotoCollection>> {
        return createPager { page, loadSize ->
            searchRemoteDataSource.searchCollections(query = query, page = page, size = loadSize)
        }
    }

    override fun searchUsersFlow(query: String): Flow<PagingData<User>> {
        return createPager { page, loadSize ->
            searchRemoteDataSource.searchUsers(query = query, page = page, size = loadSize)
        }
    }
}