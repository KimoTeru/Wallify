package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.runtime.Composable
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent

/** The language chosen in the settings. The apps pass it to `ProvideAppLocale(languageTag = …)` around [RootGraphUi]. */
@Composable
fun RootGraphComponent.languageTag(): String = getUiState().languageTag