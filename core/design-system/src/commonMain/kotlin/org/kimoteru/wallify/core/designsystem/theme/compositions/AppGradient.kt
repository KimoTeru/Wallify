package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppGradient(
    val blackOverlayGradient: List<Color> = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.72f)
    )
)

val LocalGradients = staticCompositionLocalOf { AppGradient() }