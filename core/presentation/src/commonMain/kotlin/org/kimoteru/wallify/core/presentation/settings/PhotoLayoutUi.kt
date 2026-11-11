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
    internal val domain: PhotoLayout,
    val label: StringResource,
    val gridMode: PhotoGridMode,
) {
    Masonry(PhotoLayout.Masonry, Res.string.settings_layout_masonry, PhotoGridMode.Masonry),
    Grid(PhotoLayout.Grid, Res.string.settings_layout_grid, PhotoGridMode.Grid),
    List(PhotoLayout.List, Res.string.settings_layout_list, PhotoGridMode.List),
}

fun PhotoLayoutUi.toDomain(): PhotoLayout = domain

fun PhotoLayout.toPresentation(): PhotoLayoutUi = PhotoLayoutUi.entries.first { it.domain == this }