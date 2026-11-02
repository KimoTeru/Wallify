package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppBlurs(
    val small: Dp = 10.dp,
    val medium: Dp = 20.dp,
    val large: Dp = 30.dp
)

val LocalBlurs = staticCompositionLocalOf { AppBlurs() }