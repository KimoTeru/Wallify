package org.kimoteru.wallify.core.network.model.request.photo

import org.kimoteru.wallify.core.model.photo.UpdatePhotoParams

/** Body of the photo update request, sent as a form. `null` fields are not sent. */
data class UpdatePhotoRequest(
    val description: String? = null,
    val showOnProfile: Boolean? = null,
    val tags: String? = null,
    val location: UpdatePhotoLocationRequest? = null,
    val exif: UpdatePhotoExifRequest? = null
)

data class UpdatePhotoLocationRequest(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val name: String? = null,
    val city: String? = null,
    val country: String? = null
)

data class UpdatePhotoExifRequest(
    val make: String? = null,
    val model: String? = null,
    val exposureTime: String? = null,
    val apertureValue: String? = null,
    val focalLength: String? = null,
    val isoSpeedRatings: Int? = null
)

fun UpdatePhotoParams.toRequest(): UpdatePhotoRequest = UpdatePhotoRequest(
    description = description,
    showOnProfile = showOnProfile,
    tags = tags,
    location = location?.let { place ->
        UpdatePhotoLocationRequest(
            latitude = place.latitude,
            longitude = place.longitude,
            name = place.name,
            city = place.city,
            country = place.country
        )
    },
    exif = exif?.let { camera ->
        UpdatePhotoExifRequest(
            make = camera.make,
            model = camera.model,
            exposureTime = camera.exposureTime,
            apertureValue = camera.apertureValue,
            focalLength = camera.focalLength,
            isoSpeedRatings = camera.isoSpeedRatings
        )
    }
)