package org.kimoteru.wallify.core.presentation.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.model.settings.ThemeMode
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.settings_theme_day
import org.kimoteru.wallify.core.resources.generated.resources.settings_theme_night
import org.kimoteru.wallify.core.resources.generated.resources.settings_theme_system

/** Label of a theme. */
enum class ThemeModeUi(
    val label: StringResource
) {
    System(Res.string.settings_theme_system),
    Night(Res.string.settings_theme_night),
    Day(Res.string.settings_theme_day)
}

/** Whether the night scheme is drawn: the chosen one, or the device's own for [ThemeModeUi.System]. */
@Composable
fun ThemeModeUi.isDarkTheme(): Boolean {
    val isSystemDark = isSystemInDarkTheme()

    return when (this) {
        ThemeModeUi.System -> isSystemDark
        ThemeModeUi.Night -> true
        ThemeModeUi.Day -> false
    }
}

fun ThemeModeUi.toDomain(): ThemeMode = when (this) {
    ThemeModeUi.System -> ThemeMode.System
    ThemeModeUi.Night -> ThemeMode.Night
    ThemeModeUi.Day -> ThemeMode.Day
}

fun ThemeMode.toPresentation(): ThemeModeUi = when (this) {
    ThemeMode.System -> ThemeModeUi.System
    ThemeMode.Night -> ThemeModeUi.Night
    ThemeMode.Day -> ThemeModeUi.Day
}