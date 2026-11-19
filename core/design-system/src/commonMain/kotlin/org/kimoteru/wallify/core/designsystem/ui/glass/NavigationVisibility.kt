package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

private const val SCROLL_THRESHOLD = 4f

/**
 * Whether the floating navigation (the dock) is shown. The navigation host owns it and puts
 * [rememberHideOnScrollConnection] on the container of its screens: the scroll of any list inside reaches it through
 * nested scroll, so the screens know nothing about the navigation.
 */
@Stable
class NavigationVisibility {
    var isVisible by mutableStateOf(true)
}

/** Hides the navigation while the content scrolls down and brings it back at the first scroll up. */
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