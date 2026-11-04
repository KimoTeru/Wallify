package org.kimoteru.wallify.core.designsystem.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.window.core.layout.WindowSizeClass

/** Width class of the current window: phone, foldable / small tablet, desktop / large tablet. */
enum class WindowType {
    Compact,
    Medium,
    Expanded
}

/**
 * Layout the user forced instead of the one the window width suggests; `null` means "follow the window".
 * The root provides it from the settings, [rememberWindowType] honors it, so every screen follows.
 */
val LocalWindowTypeOverride = staticCompositionLocalOf<WindowType?> { null }

/**
 * Current [WindowType]: the forced layout when the user chose one, otherwise from the window width.
 * Recomposes when the window is resized or rotated, so it also works on desktop.
 */
@Composable
fun rememberWindowType(): WindowType {
    val override = LocalWindowTypeOverride.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return override ?: when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowType.Expanded
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowType.Medium
        else -> WindowType.Compact
    }
}