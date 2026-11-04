package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private fun baseTextStyle(
    size: Int,
    lineHeight: Int,
    baseTextColor: Color,
    fontFamily: FontFamily,
    letterSpacing: TextUnit = TextUnit.Unspecified
) = TextStyle(
    fontSize = size.sp,
    fontFamily = fontFamily,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing,
    color = baseTextColor
)

data class AppTypography(
    val baseTextColor: Color = AppColors.DarkColors.text.title,
    val fontFamily: FontFamily = FontFamily.SansSerif
) {
    val h1 = TextStyles(baseTextStyle(48, 72, baseTextColor, fontFamily, (-0.035).em))
    val h2 = TextStyles(baseTextStyle(32, 44, baseTextColor, fontFamily, (-0.025).em))
    val h3 = TextStyles(baseTextStyle(24, 32, baseTextColor, fontFamily, (-0.015).em))
    val h4 = TextStyles(baseTextStyle(20, 28, baseTextColor, fontFamily, (-0.01).em))
    val h5 = TextStyles(baseTextStyle(16, 24, baseTextColor, fontFamily))
    val h6 = TextStyles(baseTextStyle(14, 20, baseTextColor, fontFamily))
    val h7 = TextStyles(baseTextStyle(12, 18, baseTextColor, fontFamily))
    val h8 = TextStyles(baseTextStyle(10, 16, baseTextColor, fontFamily))

    /** Micro labels and section headers; the text itself is uppercased by the caller. */
    val caps = TextStyles(baseTextStyle(11, 16, baseTextColor, fontFamily, 0.08.em))
}

data class TextStyles(private val base: TextStyle) {
    val regular = base.copy(fontWeight = FontWeight.Normal)
    val medium = base.copy(fontWeight = FontWeight.Medium)
    val semiBold = base.copy(fontWeight = FontWeight.SemiBold)
    val bold = base.copy(fontWeight = FontWeight.Bold)
}

val LocalTypography = staticCompositionLocalOf { AppTypography() }