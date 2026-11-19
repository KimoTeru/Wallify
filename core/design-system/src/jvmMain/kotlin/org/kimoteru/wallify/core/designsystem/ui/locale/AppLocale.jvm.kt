package org.kimoteru.wallify.core.designsystem.ui.locale

import androidx.compose.runtime.Composable
import java.util.Locale

@Composable
internal actual fun ApplyAppLocale(languageTag: String) {
    Locale.setDefault(Locale.forLanguageTag(languageTag))
}