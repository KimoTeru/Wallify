package org.kimoteru.wallify.core.designsystem.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

/** Width class of the current window: phone, foldable / small tablet, desktop / large tablet. */
enum class WindowType {
    Compact,
    Medium,
    Expanded
}

/** Current [WindowType]. Recomposes when the window is resized or rotated, so it also works on desktop. */
@Composable
fun rememberWindowType(): WindowType {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowType.Expanded
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowType.Medium
        else -> WindowType.Compact
    }
}