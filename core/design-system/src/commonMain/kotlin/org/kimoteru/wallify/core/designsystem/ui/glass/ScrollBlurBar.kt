package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged

/**
 * Full-width frosted bar, the same glass as the navigation dock, whose background follows [scrolledPx]: at the top
 * (zero) it is transparent and it reaches the full blur once the list has moved by the bar's own height. The hairline
 * is only along the bottom edge. [content] (a title, a back button) stays visible the whole time; only the background
 * fades in. The list it blurs is a
 * sibling marked with `blurSource`, not a child of this bar.
 */
@Composable
fun ScrollBlurBar(
    modifier: Modifier = Modifier,
    scrolledPx: Int,
    content: @Composable BoxScope.() -> Unit
) {
    var heightPx by remember { mutableIntStateOf(0) }
    val progress = if (heightPx == 0) 0f else (scrolledPx / heightPx.toFloat()).coerceIn(0f, 1f)

    Box(modifier = modifier.onSizeChanged { heightPx = it.height }) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = progress }
                    .glassSurface(GlassLevel.Floating, RectangleShape, edges = setOf(GlassEdge.Bottom))
            )
        }

        content()
    }
}