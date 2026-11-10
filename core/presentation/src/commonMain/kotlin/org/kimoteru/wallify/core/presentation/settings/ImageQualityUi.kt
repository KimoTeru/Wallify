package org.kimoteru.wallify.core.presentation.settings

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_high
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_low
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_medium
import org.kimoteru.wallify.core.resources.generated.resources.settings_image_quality_original

/** Label of an image quality. */
enum class ImageQualityUi(
    val label: StringResource
) {
    Low(Res.string.settings_image_quality_low),
    Medium(Res.string.settings_image_quality_medium),
    High(Res.string.settings_image_quality_high),
    Original(Res.string.settings_image_quality_original)
}

fun ImageQualityUi.toDomain(): ImageQuality = when (this) {
    ImageQualityUi.Low -> ImageQuality.Low
    ImageQualityUi.Medium -> ImageQuality.Medium
    ImageQualityUi.High -> ImageQuality.High
    ImageQualityUi.Original -> ImageQuality.Original
}

fun ImageQuality.toPresentation(): ImageQualityUi = when (this) {
    ImageQuality.Low -> ImageQualityUi.Low
    ImageQuality.Medium -> ImageQualityUi.Medium
    ImageQuality.High -> ImageQualityUi.High
    ImageQuality.Original -> ImageQualityUi.Original
}