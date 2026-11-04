package org.kimoteru.wallify.core.designsystem.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.window.core.layout.WindowSizeClass

/** The narrow side of the screen is as wide as a medium window or wider: a tablet or an unfolded foldable. */
@Composable
actual fun rememberCanUseExpandedLayout(): Boolean =
    LocalConfiguration.current.smallestScreenWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND