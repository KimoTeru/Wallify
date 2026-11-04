package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.model.user.User

interface SearchRemoteDataSource {

    /** [languageTag] is the language of the query; `null` leaves the backend default. */
    suspend fun searchPhotos(
        query: SearchPhotosQuery,
        languageTag: String?,
        page: Int,
        size: Int
    ): Response<List<Photo>>

    suspend fun searchCollections(query: String, page: Int, size: Int): Response<List<PhotoCollection>>

    suspend fun searchUsers(query: String, page: Int, size: Int): Response<List<User>>
}