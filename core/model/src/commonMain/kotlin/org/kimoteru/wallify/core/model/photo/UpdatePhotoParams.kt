package org.kimoteru.wallify.core.model.photo

/**
 * Changes of a photo of the current user; `null` fields stay as they are.
 * [tags] is a single value that is sent as it is: the backend documents no list format.
 */
data class UpdatePhotoParams(
    val description: String? = null,
    val showOnProfile: Boolean? = null,
    val tags: String? = null,
    val location: UpdatePhotoLocation? = null,
    val exif: UpdatePhotoExif? = null
)

data class UpdatePhotoLocation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val name: String? = null,
    val city: String? = null,
    val country: String? = null
)

data class UpdatePhotoExif(
    val make: String? = null,
    val model: String? = null,
    val exposureTime: String? = null,
    val apertureValue: String? = null,
    val focalLength: String? = null,
    val isoSpeedRatings: Int? = null
)