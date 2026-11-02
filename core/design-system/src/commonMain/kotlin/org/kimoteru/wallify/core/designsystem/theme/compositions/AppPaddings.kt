package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppPaddings(
    val smallExtra: Dp = 4.dp,
    val smallSpecial: Dp = 6.dp,
    val small: Dp = 8.dp,
    val mediumSpecial: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val largeSpecial: Dp = 24.dp,
    val large: Dp = 32.dp,
    val largeExtra: Dp = 48.dp
)

val LocalPaddings = staticCompositionLocalOf { AppPaddings() }