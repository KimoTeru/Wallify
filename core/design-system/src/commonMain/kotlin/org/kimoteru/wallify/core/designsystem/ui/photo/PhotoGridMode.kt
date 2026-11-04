package org.kimoteru.wallify.core.designsystem.ui.photo

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * How photo cards are laid out: [Masonry] keeps the proportions of every photo, [Grid] makes all cards the same
 * size, [List] shows large cards in one column (two on wide windows). The root provides the user's choice
 * through [LocalPhotoGridMode]; [PhotoGridLayout] and [PhotoCard] follow it.
 */
enum class PhotoGridMode {
    Masonry,
    Grid,
    List
}

val LocalPhotoGridMode = staticCompositionLocalOf { PhotoGridMode.Masonry }

/** Columns the user chose for [PhotoGridMode.Masonry] and [PhotoGridMode.Grid]; `null` lets the window decide. */
val LocalPhotoGridColumns = staticCompositionLocalOf<Int?> { null }