package org.kimoteru.wallify.core.model.collection

import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.user.User

/**
 * Collection of photos (named so to not clash with `kotlin.collections.Collection`).
 * [coverPhoto] and [user] are empty where the backend sends the short form, e.g. inside a photo.
 */
@Serializable
data class PhotoCollection(
    val id: Int,
    val title: String,
    val description: String?,
    val totalPhotos: Int,
    val isPrivate: Boolean,
    val coverPhoto: Photo?,
    val user: User?
)

/** Result of adding a photo to a collection or removing it. */
@Serializable
data class CollectionPhotoChange(
    val photo: Photo,
    val collection: PhotoCollection
)