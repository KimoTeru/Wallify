package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf

data class AppAlphas(
    val smallExtra: Float = 0.05f,
    val smallSpecial: Float = 0.1f,
    val small: Float = 0.2f,
    val mediumSpecial: Float = 0.25f,
    val medium: Float = 0.4f,
    val largeSpecial: Float = 0.6f,
    val large: Float = 0.7f,
    val largeExtra: Float = 0.8f,

    val full: Float = 1f,
    val transparent: Float = 0f
)

val LocalAlphas = staticCompositionLocalOf { AppAlphas() }