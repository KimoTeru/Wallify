package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import org.kimoteru.wallify.core.presentation.settings.ThemeModeUi
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent

/**
 * Whether the apps draw the night scheme: the one chosen in the settings, or the device's own for
 * [ThemeModeUi.System]. The apps pass it to `AppTheme(isDarkTheme = …)` around [RootGraphUi].
 */
@Composable
fun RootGraphComponent.isDarkTheme(): Boolean {
    val isSystemDark = isSystemInDarkTheme()

    return when (getUiState().themeMode) {
        ThemeModeUi.System -> isSystemDark
        ThemeModeUi.Night -> true
        ThemeModeUi.Day -> false
    }
}