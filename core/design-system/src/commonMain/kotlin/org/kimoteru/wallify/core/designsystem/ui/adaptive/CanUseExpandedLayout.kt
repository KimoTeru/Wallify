package org.kimoteru.wallify.core.designsystem.ui.adaptive

import androidx.compose.runtime.Composable

/**
 * Whether this device can show the [WindowType.Expanded] layout at all, whatever the window is right now: a desktop
 * window can always be widened, an Android device only if its screen is as wide as a tablet or an unfolded foldable
 * (a phone never reaches it). A choice of the screen layout is shown only where this is `true`.
 */
@Composable
expect fun rememberCanUseExpandedLayout(): Boolean