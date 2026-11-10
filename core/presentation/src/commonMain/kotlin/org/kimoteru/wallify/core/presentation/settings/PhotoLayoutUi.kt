package org.kimoteru.wallify.core.presentation.settings

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.model.settings.PhotoLayout
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_grid
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_list
import org.kimoteru.wallify.core.resources.generated.resources.settings_layout_masonry

/** Label and grid mode of a photo list layout. */
enum class PhotoLayoutUi(
    val label: StringResource,
    val gridMode: PhotoGridMode
) {
    Masonry(Res.string.settings_layout_masonry, PhotoGridMode.Masonry),
    Grid(Res.string.settings_layout_grid, PhotoGridMode.Grid),
    List(Res.string.settings_layout_list, PhotoGridMode.List)
}

fun PhotoLayoutUi.toDomain(): PhotoLayout = when (this) {
    PhotoLayoutUi.Masonry -> PhotoLayout.Masonry
    PhotoLayoutUi.Grid -> PhotoLayout.Grid
    PhotoLayoutUi.List -> PhotoLayout.List
}

fun PhotoLayout.toPresentation(): PhotoLayoutUi = when (this) {
    PhotoLayout.Masonry -> PhotoLayoutUi.Masonry
    PhotoLayout.Grid -> PhotoLayoutUi.Grid
    PhotoLayout.List -> PhotoLayoutUi.List
}