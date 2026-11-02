package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppBorderWidths(
    val small: Dp = 1.dp,
    val medium: Dp = 2.dp,
    val large: Dp = 3.dp
)

val LocalBorderWidths = staticCompositionLocalOf { AppBorderWidths() }