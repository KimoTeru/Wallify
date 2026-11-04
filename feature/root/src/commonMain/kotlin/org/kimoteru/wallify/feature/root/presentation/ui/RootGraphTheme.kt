package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import org.kimoteru.wallify.core.model.settings.ThemeMode
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent

/**
 * Whether the apps draw the night scheme: the one chosen in the settings, or the device's own for
 * [ThemeMode.System]. The apps pass it to `AppTheme(isDarkTheme = …)` around [RootGraphUi].
 */
@Composable
fun RootGraphComponent.isDarkTheme(): Boolean {
    val isSystemDark = isSystemInDarkTheme()

    return when (getUiState().themeMode) {
        ThemeMode.System -> isSystemDark
        ThemeMode.Night -> true
        ThemeMode.Day -> false
    }
}