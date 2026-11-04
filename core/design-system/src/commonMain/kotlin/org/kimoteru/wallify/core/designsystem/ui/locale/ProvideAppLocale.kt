package org.kimoteru.wallify.core.designsystem.ui.locale

import androidx.compose.runtime.Composable

/**
 * Shows [content] in the language [languageTag] (`null` is the language of the device): every string resource under
 * it is read in that language. The content is built again when the language changes, so the screens switch at once.
 * The root puts the whole app inside, the apps never choose a language themselves.
 */
@Composable
expect fun ProvideAppLocale(languageTag: String?, content: @Composable () -> Unit)