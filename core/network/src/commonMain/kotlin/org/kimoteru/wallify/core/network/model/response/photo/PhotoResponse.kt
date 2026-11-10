package org.kimoteru.wallify.core.network.model.response.photo

import io.ktor.http.URLBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.common.extensions.CommaSpace
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.common.extensions.nullIfBlank
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoExif
import org.kimoteru.wallify.core.model.photo.PhotoUrls
import org.kimoteru.wallify.core.network.model.response.collection.CollectionResponse
import org.kimoteru.wallify.core.network.model.response.stats.PhotoStatisticsResponse
import org.kimoteru.wallify.core.network.model.response.user.UserResponse
import org.kimoteru.wallify.core.network.model.response.user.toDomain
import org.kimoteru.wallify.core.network.model.response.withReferral

private const val IMAGE_WIDTH = "w"
private const val IMAGE_FORMAT = "fm"
private const val IMAGE_FORMAT_JPG = "jpg"
private const val IMAGE_DOWNLOAD = "dl"
private const val THUMB_WIDTH = 200
private const val SMALL_WIDTH = 400
private const val REGULAR_WIDTH = 1080

/**
 * Photo as the backend sends it. List endpoints return a summary, so the detail-only fields are `null` there.
 * [urls] are the hotlinked image URLs.
 */
@Serializable
data class PhotoResponse(
    @SerialName("id")
    val id: String,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("promoted_at")
    val promotedAt: String? = null,

    @SerialName("width")
    val width: Int? = null,

    @SerialName("height")
    val height: Int? = null,

    @SerialName("color")
    val color: String? = null,

    @SerialName("blur_hash")
    val blurHash: String? = null,

    @SerialName("downloads")
    val downloads: Int? = null,

    @SerialName("public_domain")
    val publicDomain: Boolean? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("alt_description")
    val altDescription: String? = null,

    @SerialName("exif")
    val exif: ExifResponse? = null,

    @SerialName("location")
    val location: PhotoLocationResponse? = null,

    @SerialName("tags")
    val tags: List<TagResponse> = emptyList(),

    @SerialName("current_user_collections")
    val currentUserCollections: List<CollectionResponse> = emptyList(),

    @SerialName("urls")
    val urls: PhotoUrlsResponse,

    @SerialName("links")
    val links: PhotoLinksResponse? = null,

    @SerialName("user")
    val user: UserResponse? = null,

    @SerialName("statistics")
    val statistics: PhotoStatisticsResponse? = null
)

@Serializable
data class PhotoUrlsResponse(
    @SerialName("raw")
    val raw: String,

    @SerialName("full")
    val full: String? = null,

    @SerialName("regular")
    val regular: String? = null,

    @SerialName("small")
    val small: String? = null,

    @SerialName("thumb")
    val thumb: String? = null
)

@Serializable
data class PhotoLinksResponse(
    @SerialName("self")
    val self: String? = null,

    @SerialName("html")
    val html: String? = null,

    @SerialName("download")
    val download: String? = null,

    @SerialName("download_location")
    val downloadLocation: String? = null
)

@Serializable
data class ExifResponse(
    @SerialName("make")
    val make: String? = null,

    @SerialName("model")
    val model: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("exposure_time")
    val exposureTime: String? = null,

    @SerialName("aperture")
    val aperture: String? = null,

    @SerialName("focal_length")
    val focalLength: String? = null,

    @SerialName("iso")
    val iso: Int? = null
)

@Serializable
data class PhotoLocationResponse(
    @SerialName("name")
    val name: String? = null,

    @SerialName("city")
    val city: String? = null,

    @SerialName("country")
    val country: String? = null,

    @SerialName("position")
    val position: PositionResponse? = null
)

@Serializable
data class PositionResponse(
    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null
)

@Serializable
data class TagResponse(
    @SerialName("title")
    val title: String
)

/** Answer of the download tracking endpoint: the URL of the file to download. */
@Serializable
data class PhotoDownloadResponse(
    @SerialName("url")
    val url: String
)

/**
 * Address of the file of [photoId] in [quality]: the original as the backend answers it, the other levels scaled
 * down by the image service to the widths of the hotlinked sizes. `dl` makes the browser save the file instead of
 * showing it.
 */
fun PhotoDownloadResponse.toFileUrl(photoId: String, quality: ImageQuality): String =
    URLBuilder(url).apply {
        quality.toWidth()?.let { width -> parameters.append(IMAGE_WIDTH, width.toString()) }
        parameters.append(IMAGE_FORMAT, IMAGE_FORMAT_JPG)
        parameters.append(IMAGE_DOWNLOAD, "$photoId.$IMAGE_FORMAT_JPG")
    }.buildString()

/** Widths of `thumb`, `small` and `regular`; the original keeps its own size. */
private fun ImageQuality.toWidth(): Int? = when (this) {
    ImageQuality.Low -> THUMB_WIDTH
    ImageQuality.Medium -> SMALL_WIDTH
    ImageQuality.High -> REGULAR_WIDTH
    ImageQuality.Original -> null
}

fun PhotoResponse.toDomain(): Photo = Photo(
    id = id,
    width = width ?: 0,
    height = height ?: 0,
    color = color ?: String.Empty,
    blurHash = blurHash,
    description = description?.nullIfBlank() ?: altDescription?.nullIfBlank(),
    urls = urls.toDomain(),
    htmlUrl = links?.html?.withReferral(),
    downloadLocation = links?.downloadLocation,
    location = location?.toDisplayName(),
    tags = tags.map { tag -> tag.title },
    downloads = downloads,
    exif = exif?.toDomain(),
    currentUserCollectionIds = currentUserCollections.map { collection -> collection.id },
    user = user?.toDomain()
)

fun ExifResponse.toDomain(): PhotoExif = PhotoExif(
    make = make?.nullIfBlank(),
    model = model?.nullIfBlank(),
    exposureTime = exposureTime?.nullIfBlank(),
    aperture = aperture?.nullIfBlank(),
    focalLength = focalLength?.nullIfBlank(),
    iso = iso
)

/** Sizes the backend left out fall back to [PhotoUrlsResponse.raw], so the UI always has a URL to show. */
fun PhotoUrlsResponse.toDomain(): PhotoUrls = PhotoUrls(
    raw = raw,
    full = full ?: raw,
    regular = regular ?: raw,
    small = small ?: raw,
    thumb = thumb ?: raw
)

private fun PhotoLocationResponse.toDisplayName(): String? =
    name?.nullIfBlank() ?: listOfNotNull(city, country).joinToString(String.CommaSpace).nullIfBlank()