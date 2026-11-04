package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

private const val SCROLL_THRESHOLD = 4f

/**
 * Whether the bars that float over a scrolling screen (the dock and the screen's own top bar) are shown.
 * The navigation host owns it and provides it through [LocalNavigationVisibility]; a screen with a long list
 * feeds it with [rememberHideOnScrollConnection] and shows its top bar while [isVisible].
 */
@Stable
class NavigationVisibility {
    var isVisible by mutableStateOf(true)
}

val LocalNavigationVisibility = staticCompositionLocalOf { NavigationVisibility() }

/** Hides the bars while the content scrolls down and brings them back at the first scroll up. */
@Composable
fun rememberHideOnScrollConnection(visibility: NavigationVisibility): NestedScrollConnection =
    remember(visibility) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -SCROLL_THRESHOLD) {
                    visibility.isVisible = false
                } else if (available.y > SCROLL_THRESHOLD) {
                    visibility.isVisible = true
                }
                return Offset.Zero
            }
        }
    }