package org.kimoteru.wallify.core.designsystem.ui.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * The string resources follow the default locale and the configuration of the composition, so both are changed; the
 * context stays the activity, only its configuration seen by Compose is replaced.
 */
@Composable
actual fun ProvideAppLocale(languageTag: String, content: @Composable () -> Unit) {
    val locale = Locale.forLanguageTag(languageTag)
    val configuration = LocalConfiguration.current

    Locale.setDefault(locale)

    val localized = remember(configuration, locale) {
        Configuration(configuration).apply { setLocale(locale) }
    }

    CompositionLocalProvider(LocalConfiguration provides localized) {
        key(languageTag) {
            content()
        }
    }
}