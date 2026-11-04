package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/**
 * The top bar of a scrolling screen: [bar] stays pinned, its background is [ScrollBlurBar] (transparent until
 * [scrolledPx] grows, then the same frosted glass as the dock). The feed, settings, the photo, topics and random use it;
 * a screen only fills [bar] and passes how far its list has scrolled.
 *
 * [bar] brings its own top margin (status bar included, see [rememberScreenPadding]). [content] gets the height the
 * list keeps free above its first item as `topPadding`. The navigation dock still hides on scroll down.
 */
@Composable
fun FloatingBarScreen(
    modifier: Modifier = Modifier,
    scrolledPx: Int,
    bar: @Composable () -> Unit,
    content: @Composable (topPadding: Dp) -> Unit
) {
    val appPadding = AppTheme.padding

    val density = LocalDensity.current
    val visibility = LocalNavigationVisibility.current
    var barHeight by remember { mutableStateOf(Dp.Hairline) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberHideOnScrollConnection(visibility))
    ) {
        content(barHeight + appPadding.medium)

        ScrollBlurBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { size -> barHeight = with(density) { size.height.toDp() } },
            scrolledPx = scrolledPx
        ) {
            Box(modifier = Modifier.padding(bottom = appPadding.medium)) {
                bar()
            }
        }
    }
}

/**
 * How far a staggered grid has scrolled. Once the first item has left, the exact distance is gone, and the bar only
 * needs to know that the blur is complete.
 */
fun LazyStaggeredGridState.scrolledPx(): Int =
    if (firstVisibleItemIndex > 0) Int.MAX_VALUE else firstVisibleItemScrollOffset