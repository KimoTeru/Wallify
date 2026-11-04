package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.runtime.Composable
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent

/**
 * The language chosen in the settings, `null` for the language of the device. The apps pass it to
 * `ProvideAppLocale(languageTag = …)` around [RootGraphUi].
 */
@Composable
fun RootGraphComponent.languageTag(): String? = getUiState().languageTag