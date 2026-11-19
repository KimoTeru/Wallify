package org.kimoteru.wallify.core.designsystem.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * Android makes the locale of the configuration the default on every configuration change (rotation, system theme),
 * so the configuration is read here: after the change the app locale replaces it before the content recomposes.
 */
@Composable
internal actual fun ApplyAppLocale(languageTag: String) {
    val configuration = LocalConfiguration.current

    remember(configuration, languageTag) {
        Locale.forLanguageTag(languageTag).also(Locale::setDefault)
    }
}