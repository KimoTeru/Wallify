package org.kimoteru.wallify.core.model.photo

import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.user.User

/**
 * Photo with what the app shows and needs for a download. List endpoints send a summary, so [downloads] and [exif]
 * are `null` and [tags] and [currentUserCollectionIds] are empty there: use `PhotoRepository.getPhoto` for the
 * full photo.
 * [htmlUrl] is the photo page that attribution links to. [downloadLocation] is the address that must be requested
 * whenever the app downloads the photo (`PhotoRepository.trackDownload`).
 * [currentUserCollectionIds] are the collections of the signed-in user that already hold the photo.
 */
@Serializable
data class Photo(
    val id: String,
    val width: Int,
    val height: Int,
    val color: String,
    val blurHash: String?,
    val description: String?,
    val urls: PhotoUrls,
    val htmlUrl: String?,
    val downloadLocation: String?,
    val location: String?,
    val tags: List<String>,
    val downloads: Int?,
    val exif: PhotoExif?,
    val currentUserCollectionIds: List<Int>,
    val user: User?
)

/** Camera data of a photo; every field is `null` when the photographer did not share it. */
@Serializable
data class PhotoExif(
    val make: String?,
    val model: String?,
    val exposureTime: String?,
    val aperture: String?,
    val focalLength: String?,
    val iso: Int?
)

/** Hotlinked image URLs. Use them as they are; build other sizes from [raw] and keep its `ixid` parameter. */
@Serializable
data class PhotoUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)