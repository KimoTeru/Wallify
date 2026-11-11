package org.kimoteru.wallify.core.presentation.photo

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.orientation_landscape
import org.kimoteru.wallify.core.resources.generated.resources.orientation_portrait
import org.kimoteru.wallify.core.resources.generated.resources.orientation_squarish

/** Label and icon of a photo orientation. */
enum class PhotoOrientationUi(
    internal val domain: PhotoOrientation,
    val label: StringResource,
    val icon: ImageVector,
) {
    Landscape(PhotoOrientation.Landscape, Res.string.orientation_landscape, AppIcons.CropLandscape),
    Portrait(PhotoOrientation.Portrait, Res.string.orientation_portrait, AppIcons.CropPortrait),
    Squarish(PhotoOrientation.Squarish, Res.string.orientation_squarish, AppIcons.CropSquare),
}

fun PhotoOrientationUi.toDomain(): PhotoOrientation = domain

fun PhotoOrientation.toPresentation(): PhotoOrientationUi = PhotoOrientationUi.entries.first { it.domain == this }