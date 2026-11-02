package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf

data class AppScales(
    val base: Float = 1f,
    val smallExtra: Float = 1.1f,
    val small: Float = 1.25f,
    val medium: Float = 1.5f,
    val largeExtra: Float = 1.6f,
    val largeSpecial: Float = 2f,
    val large: Float = 3f
)

val LocalScales = staticCompositionLocalOf { AppScales() }