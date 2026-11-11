package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberIsShortWindow
import org.kimoteru.wallify.core.designsystem.ui.blur.blurSource

/**
 * The top bar of a scrolling screen: [bar] stays pinned, its background is [ScrollGlassBar] (transparent until
 * [scrolledPx] grows, then the same frosted glass as the dock). The feed, settings, the photo, topics and random
 * use it; a screen only fills [bar] and passes how far its list has scrolled as a lambda
 * (`{ gridState.scrolledPx() }`), which the bar reads only while drawing. [content] is the blur source of the screen
 * (`blurSource`): the screen marks nothing itself.
 *
 * [bar] brings its own top margin (status bar included, see [rememberScreenPadding]). [content] gets the height the
 * list keeps free above its first item as `topPadding`. The bar stays; the dock hides on scroll down by itself (its
 * host listens to the nested scroll of the screens).
 *
 * The bar is measured before the content is composed, in the same frame (the way `Scaffold` does it), so the list
 * gets its real top padding on the very first frame. With the height kept in a state from `onSizeChanged` the first
 * frame of every opened tab had no room for the bar, and the list jumped down on the next one.
 */
@Composable
fun FloatingBarScreen(
    modifier: Modifier = Modifier,
    scrolledPx: () -> Int,
    bar: @Composable () -> Unit,
    content: @Composable (topPadding: Dp) -> Unit
) {
    val appPadding = AppTheme.padding
    val barGap = if (rememberIsShortWindow()) appPadding.small else appPadding.medium

    SubcomposeLayout(modifier = modifier.fillMaxSize()) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val barPlaceables = subcompose(FloatingBarSlot.Bar) {
            ScrollGlassBar(scrolledPx = scrolledPx) {
                Box(modifier = Modifier.padding(bottom = barGap)) {
                    bar()
                }
            }
        }.map { measurable -> measurable.measure(looseConstraints) }

        val barHeight = barPlaceables.maxOfOrNull { placeable -> placeable.height } ?: 0

        // The content is what the bar, the dock and the sheets blur; the bar is its sibling, not its child.
        val contentPlaceables = subcompose(FloatingBarSlot.Content) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blurSource()
            ) {
                content(barHeight.toDp() + barGap)
            }
        }.map { measurable -> measurable.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceables.forEach { placeable -> placeable.place(0, 0) }
            barPlaceables.forEach { placeable -> placeable.place((constraints.maxWidth - placeable.width) / 2, 0) }
        }
    }
}

/**
 * How far a staggered grid has scrolled. Once the first item has left, the exact distance is gone, and the bar only
 * needs to know that its background is fully shown.
 */
fun LazyStaggeredGridState.scrolledPx(): Int =
    if (firstVisibleItemIndex > 0) Int.MAX_VALUE else firstVisibleItemScrollOffset

private enum class FloatingBarSlot {
    Bar,
    Content,
}