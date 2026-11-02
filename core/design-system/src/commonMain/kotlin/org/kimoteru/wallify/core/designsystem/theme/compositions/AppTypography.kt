package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseFont = FontFamily.SansSerif

private fun baseTextStyle(size: Int, lineHeight: Int, baseTextColor: Color) = TextStyle(
    fontSize = size.sp,
    fontFamily = baseFont,
    lineHeight = lineHeight.sp,
    color = baseTextColor
)

data class AppTypography(
    val baseTextColor: Color = AppColors.DarkColors.text.title
) {
    val h1 = TextStyles(baseTextStyle(48, 72, baseTextColor))
    val h2 = TextStyles(baseTextStyle(32, 48, baseTextColor))
    val h3 = TextStyles(baseTextStyle(24, 36, baseTextColor))
    val h4 = TextStyles(baseTextStyle(20, 30, baseTextColor))
    val h5 = TextStyles(baseTextStyle(16, 24, baseTextColor))
    val h6 = TextStyles(baseTextStyle(14, 20, baseTextColor))
    val h7 = TextStyles(baseTextStyle(12, 18, baseTextColor))
    val h8 = TextStyles(baseTextStyle(10, 16, baseTextColor))
}

data class TextStyles(private val base: TextStyle) {
    val regular = base.copy(fontWeight = FontWeight.Normal)
    val medium = base.copy(fontWeight = FontWeight.Medium)
    val semiBold = base.copy(fontWeight = FontWeight.SemiBold)
    val bold = base.copy(fontWeight = FontWeight.Bold)
}

val LocalTypography = staticCompositionLocalOf { AppTypography() }