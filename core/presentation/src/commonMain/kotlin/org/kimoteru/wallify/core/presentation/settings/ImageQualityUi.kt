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
    internal val domain: ImageQuality,
    val label: StringResource,
) {
    Low(ImageQuality.Low, Res.string.settings_image_quality_low),
    Medium(ImageQuality.Medium, Res.string.settings_image_quality_medium),
    High(ImageQuality.High, Res.string.settings_image_quality_high),
    Original(ImageQuality.Original, Res.string.settings_image_quality_original),
}

fun ImageQualityUi.toDomain(): ImageQuality = domain

fun ImageQuality.toPresentation(): ImageQualityUi = ImageQualityUi.entries.first { it.domain == this }