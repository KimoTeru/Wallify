package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.network.api.SearchApi
import org.kimoteru.wallify.core.network.model.response.search.toDomain
import org.koin.core.annotation.Singleton

@Singleton
class SearchRemoteDataSourceImpl(
    private val searchApi: SearchApi
) : SearchRemoteDataSource {

    override suspend fun searchPhotos(
        query: SearchPhotosQuery,
        languageTag: String?,
        page: Int,
        size: Int
    ): Response<List<Photo>> {
        return searchApi.searchPhotos(query = query, languageTag = languageTag, page = page, perPage = size)
            .map { it.toDomain() }
    }

    override suspend fun searchCollections(query: String, page: Int, size: Int): Response<List<PhotoCollection>> {
        return searchApi.searchCollections(query = query, page = page, perPage = size)
            .map { it.toDomain() }
    }

    override suspend fun searchUsers(query: String, page: Int, size: Int): Response<List<User>> {
        return searchApi.searchUsers(query = query, page = page, perPage = size)
            .map { it.toDomain() }
    }
}