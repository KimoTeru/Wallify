package org.kimoteru.wallify.core.designsystem.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Language of the app that [org.kimoteru.wallify.core.designsystem.theme.AppTheme] provides. It is static on purpose:
 * a new value recomposes the whole content, so every string resource is read again in the new language, while the
 * state of the screens (scroll, remembered values, the stack) stays. `key(language)` would build the content anew and
 * the UI would jump.
 */
internal val LocalAppLocale = staticCompositionLocalOf<String?> { null }

/** Makes [languageTag] the default locale, which the string resources read; it runs before the content. */
@Composable
internal expect fun ApplyAppLocale(languageTag: String)