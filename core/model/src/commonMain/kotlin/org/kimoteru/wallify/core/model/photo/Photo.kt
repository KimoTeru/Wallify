package org.kimoteru.wallify.core.model.photo

import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.user.User

/**
 * Photo with what the app shows and needs for a download. List endpoints send a summary, so [downloads] is `null`
 * and [tags] is empty there: use `PhotoRepository.getPhoto` for the full photo.
 * [htmlUrl] is the photo page that attribution links to. [downloadLocation] is the address that must be requested
 * whenever the app downloads the photo (`PhotoRepository.trackDownload`).
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
    val user: User?
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