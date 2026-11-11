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
    internal val domain: ThemeMode,
    val label: StringResource,
) {
    System(ThemeMode.System, Res.string.settings_theme_system),
    Night(ThemeMode.Night, Res.string.settings_theme_night),
    Day(ThemeMode.Day, Res.string.settings_theme_day),
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

fun ThemeModeUi.toDomain(): ThemeMode = domain

fun ThemeMode.toPresentation(): ThemeModeUi = ThemeModeUi.entries.first { it.domain == this }