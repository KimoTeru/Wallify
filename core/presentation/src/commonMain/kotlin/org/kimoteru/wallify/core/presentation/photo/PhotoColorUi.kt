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
    val label: StringResource
) {
    BlackAndWhite(Res.string.color_black_and_white),
    Black(Res.string.color_black),
    White(Res.string.color_white),
    Yellow(Res.string.color_yellow),
    Orange(Res.string.color_orange),
    Red(Res.string.color_red),
    Purple(Res.string.color_purple),
    Magenta(Res.string.color_magenta),
    Green(Res.string.color_green),
    Teal(Res.string.color_teal),
    Blue(Res.string.color_blue)
}

fun PhotoColorUi.toDomain(): PhotoColor = when (this) {
    PhotoColorUi.BlackAndWhite -> PhotoColor.BlackAndWhite
    PhotoColorUi.Black -> PhotoColor.Black
    PhotoColorUi.White -> PhotoColor.White
    PhotoColorUi.Yellow -> PhotoColor.Yellow
    PhotoColorUi.Orange -> PhotoColor.Orange
    PhotoColorUi.Red -> PhotoColor.Red
    PhotoColorUi.Purple -> PhotoColor.Purple
    PhotoColorUi.Magenta -> PhotoColor.Magenta
    PhotoColorUi.Green -> PhotoColor.Green
    PhotoColorUi.Teal -> PhotoColor.Teal
    PhotoColorUi.Blue -> PhotoColor.Blue
}

fun PhotoColor.toPresentation(): PhotoColorUi = when (this) {
    PhotoColor.BlackAndWhite -> PhotoColorUi.BlackAndWhite
    PhotoColor.Black -> PhotoColorUi.Black
    PhotoColor.White -> PhotoColorUi.White
    PhotoColor.Yellow -> PhotoColorUi.Yellow
    PhotoColor.Orange -> PhotoColorUi.Orange
    PhotoColor.Red -> PhotoColorUi.Red
    PhotoColor.Purple -> PhotoColorUi.Purple
    PhotoColor.Magenta -> PhotoColorUi.Magenta
    PhotoColor.Green -> PhotoColorUi.Green
    PhotoColor.Teal -> PhotoColorUi.Teal
    PhotoColor.Blue -> PhotoColorUi.Blue
}