package org.kimoteru.wallify.core.network.model.response.collection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.kimoteru.wallify.core.network.model.response.user.UserResponse
import org.kimoteru.wallify.core.network.model.response.user.toDomain

/** Collection. `coverPhoto` and `user` are `null` where the backend sends a short form (e.g. inside a photo). */
@Serializable
data class CollectionResponse(
    @SerialName("id")
    val id: Int,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("published_at")
    val publishedAt: String? = null,

    @SerialName("last_collected_at")
    val lastCollectedAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("featured")
    val featured: Boolean? = null,

    @SerialName("total_photos")
    val totalPhotos: Int? = null,

    @SerialName("private")
    val isPrivate: Boolean? = null,

    @SerialName("share_key")
    val shareKey: String? = null,

    @SerialName("cover_photo")
    val coverPhoto: PhotoResponse? = null,

    @SerialName("user")
    val user: UserResponse? = null,

    @SerialName("links")
    val links: CollectionLinksResponse? = null
)

@Serializable
data class CollectionLinksResponse(
    @SerialName("self")
    val self: String? = null,

    @SerialName("html")
    val html: String? = null,

    @SerialName("photos")
    val photos: String? = null,

    @SerialName("related")
    val related: String? = null
)

/** Answer of the add and remove photo endpoints. */
@Serializable
data class CollectionPhotoChangeResponse(
    @SerialName("photo")
    val photo: PhotoResponse,

    @SerialName("collection")
    val collection: CollectionResponse,

    @SerialName("user")
    val user: UserResponse? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

fun CollectionResponse.toDomain(): PhotoCollection = PhotoCollection(
    id = id,
    title = title,
    description = description,
    totalPhotos = totalPhotos ?: 0,
    isPrivate = isPrivate ?: false,
    coverPhoto = coverPhoto?.toDomain(),
    user = user?.toDomain()
)

fun CollectionPhotoChangeResponse.toDomain(): CollectionPhotoChange = CollectionPhotoChange(
    photo = photo.toDomain(),
    collection = collection.toDomain()
)