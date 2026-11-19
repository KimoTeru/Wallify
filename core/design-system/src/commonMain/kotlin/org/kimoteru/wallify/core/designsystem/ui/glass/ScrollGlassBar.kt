package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Full-width bar with the same frosted glass as the navigation dock, whose background follows [scrolledPx]: at the
 * top (zero) it is transparent and it is fully shown once the list has moved by the bar's own height. The hairline
 * is only along the bottom edge. [content] (a title, a back button) stays visible the whole time; only the
 * background fades in. The list it blurs is a sibling marked with `blurSource`, not a child of this bar.
 *
 * [scrolledPx] is a lambda read in the draw phase: the scroll changes on every frame, and reading it in the
 * composition would compose the whole screen again on every pixel. The composition only learns whether the list is
 * scrolled at all: the glass and its blur exist only then, not hidden by transparency.
 */
@Composable
fun ScrollGlassBar(
    modifier: Modifier = Modifier,
    scrolledPx: () -> Int,
    content: @Composable BoxScope.() -> Unit
) {
    // The screen may pass another lambda (the photo switches its scroll with the window); the derived state follows.
    val currentScrolledPx by rememberUpdatedState(scrolledPx)
    val isScrolled by remember { derivedStateOf { currentScrolledPx() > 0 } }

    Box(modifier = modifier) {
        if (isScrolled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    // The size of the layer is known here already: a size kept from `onSizeChanged` arrives a frame
                    // later, and a scrolled tab opened again would show the bar without its glass for that frame.
                    .graphicsLayer {
                        alpha = if (size.height <= 0f) 0f else (currentScrolledPx() / size.height).coerceIn(0f, 1f)
                    }
                    .glassSurface(GlassLevel.Frosted, RectangleShape, edges = setOf(GlassEdge.Bottom))
            )
        }

        content()
    }
}