package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppElevations(
    val small: Dp = 4.dp,
    val smallSpecial: Dp = 8.dp,
    val medium: Dp = 10.dp,
    val large: Dp = 14.dp,
    val largeExtra: Dp = 24.dp
)

val LocalElevations = staticCompositionLocalOf { AppElevations() }