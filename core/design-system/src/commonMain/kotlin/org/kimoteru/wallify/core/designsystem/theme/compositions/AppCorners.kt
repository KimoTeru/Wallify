package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppCorners(
    val smallSpecial: Dp = 4.dp,
    val small: Dp = 8.dp,
    val mediumSpecial: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val specialLarge: Dp = 22.dp,
    val large: Dp = 24.dp
)

val LocalCorners = staticCompositionLocalOf { AppCorners() }