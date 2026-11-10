package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.runtime.Composable
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent

/** Whether the saved settings are read, so [requestedWindowType] is the user's choice and not a default. */
@Composable
fun RootGraphComponent.isSettingsLoaded(): Boolean = getUiState().isSettingsLoaded

/**
 * The window type the user picked as the screen layout, `null` for Auto. A desktop window resizes itself to it;
 * on a change only, so the saved window size is not overwritten at the start.
 */
@Composable
fun RootGraphComponent.requestedWindowType(): WindowType? = getUiState().screenLayout.windowType