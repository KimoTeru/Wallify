package org.kimoteru.wallify.core.presentation.photo

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.color_black
import org.kimoteru.wallify.core.resources.generated.resources.color_black_and_white
import org.kimoteru.wallify.core.resources.generated.resources.color_blue
import org.kimoteru.wallify.core.resources.generated.resources.color_green
import org.kimoteru.wallify.core.resources.generated.resources.color_magenta
import org.kimoteru.wallify.core.resources.generated.resources.color_orange
import org.kimoteru.wallify.core.resources.generated.resources.color_purple
import org.kimoteru.wallify.core.resources.generated.resources.color_red
import org.kimoteru.wallify.core.resources.generated.resources.color_teal
import org.kimoteru.wallify.core.resources.generated.resources.color_white
import org.kimoteru.wallify.core.resources.generated.resources.color_yellow

/** Label of a photo color. */
enum class PhotoColorUi(
    internal val domain: PhotoColor,
    val label: StringResource,
) {
    BlackAndWhite(PhotoColor.BlackAndWhite, Res.string.color_black_and_white),
    Black(PhotoColor.Black, Res.string.color_black),
    White(PhotoColor.White, Res.string.color_white),
    Yellow(PhotoColor.Yellow, Res.string.color_yellow),
    Orange(PhotoColor.Orange, Res.string.color_orange),
    Red(PhotoColor.Red, Res.string.color_red),
    Purple(PhotoColor.Purple, Res.string.color_purple),
    Magenta(PhotoColor.Magenta, Res.string.color_magenta),
    Green(PhotoColor.Green, Res.string.color_green),
    Teal(PhotoColor.Teal, Res.string.color_teal),
    Blue(PhotoColor.Blue, Res.string.color_blue),
}

fun PhotoColorUi.toDomain(): PhotoColor = domain

fun PhotoColor.toPresentation(): PhotoColorUi = PhotoColorUi.entries.first { it.domain == this }