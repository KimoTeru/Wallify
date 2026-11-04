package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.model.user.User

interface SearchRepository {

    fun searchPhotosFlow(query: SearchPhotosQuery): Flow<PagingData<Photo>>

    fun searchCollectionsFlow(query: String): Flow<PagingData<PhotoCollection>>

    fun searchUsersFlow(query: String): Flow<PagingData<User>>
}