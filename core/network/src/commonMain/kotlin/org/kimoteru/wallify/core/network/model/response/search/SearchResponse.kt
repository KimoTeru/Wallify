package org.kimoteru.wallify.core.network.model.response.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.network.model.response.collection.CollectionResponse
import org.kimoteru.wallify.core.network.model.response.collection.toDomain
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.kimoteru.wallify.core.network.model.response.user.UserResponse
import org.kimoteru.wallify.core.network.model.response.user.toDomain

@Serializable
data class SearchPhotosResponse(
    @SerialName("total")
    val total: Int = 0,

    @SerialName("total_pages")
    val totalPages: Int = 0,

    @SerialName("results")
    val results: List<PhotoResponse> = emptyList()
)

@Serializable
data class SearchCollectionsResponse(
    @SerialName("total")
    val total: Int = 0,

    @SerialName("total_pages")
    val totalPages: Int = 0,

    @SerialName("results")
    val results: List<CollectionResponse> = emptyList()
)

@Serializable
data class SearchUsersResponse(
    @SerialName("total")
    val total: Int = 0,

    @SerialName("total_pages")
    val totalPages: Int = 0,

    @SerialName("results")
    val results: List<UserResponse> = emptyList()
)

fun SearchPhotosResponse.toDomain(): List<Photo> =
    results.map { photo -> photo.toDomain() }

fun SearchCollectionsResponse.toDomain(): List<PhotoCollection> =
    results.map { collection -> collection.toDomain() }

fun SearchUsersResponse.toDomain(): List<User> =
    results.map { user -> user.toDomain() }