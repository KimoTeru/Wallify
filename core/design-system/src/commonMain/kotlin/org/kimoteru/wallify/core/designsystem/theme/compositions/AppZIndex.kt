package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf

data class AppZIndex(
    val dialogs: Float = 2f,
    val notifications: Float = 1f,
    val bottomSheet: Float = 1.5f,
    val dropdown: Float = 1.25f
)

val LocalZIndex = staticCompositionLocalOf { AppZIndex() }