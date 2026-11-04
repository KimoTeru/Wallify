package org.kimoteru.wallify.core.designsystem.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import java.util.Locale

/** The string resources read the default locale when they are composed, so it is set before the content is built. */
@Composable
actual fun ProvideAppLocale(languageTag: String, content: @Composable () -> Unit) {
    Locale.setDefault(Locale.forLanguageTag(languageTag))

    key(languageTag) {
        content()
    }
}