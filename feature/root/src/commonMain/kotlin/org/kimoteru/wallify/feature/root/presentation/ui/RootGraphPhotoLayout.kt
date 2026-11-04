package org.kimoteru.wallify.feature.root.presentation.ui

import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.model.settings.PhotoLayout

/** The design system knows the layouts by its own enum, so the user's choice is translated at the root. */
internal fun PhotoLayout.toGridMode(): PhotoGridMode = when (this) {
    PhotoLayout.Masonry -> PhotoGridMode.Masonry
    PhotoLayout.Grid -> PhotoGridMode.Grid
    PhotoLayout.List -> PhotoGridMode.List
}