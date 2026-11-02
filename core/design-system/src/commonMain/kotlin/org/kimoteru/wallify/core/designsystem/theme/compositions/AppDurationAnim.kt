package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf

data class AppDurationAnim(
    val smallExtra: Int = 200,
    val small: Int = 300,
    val medium: Int = 500,
    val large: Int = 1000,
    val largeExtra: Int = 2000
) {
    companion object {
        val AnimOff = AppDurationAnim(
            smallExtra = 0,
            small = 0,
            medium = 0
        )
    }
}

val LocalDurationAnim = staticCompositionLocalOf { AppDurationAnim() }