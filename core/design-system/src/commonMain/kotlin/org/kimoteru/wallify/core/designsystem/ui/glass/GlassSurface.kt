package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.blur.frostedBackdrop
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfComposable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull

/**
 * Opacity step of a glass surface: recessed panes, regular cards and panels, raised controls, controls floating
 * over content ([Floating]: a dense translucent tone, no blur of their own), the bars of a screen ([Frosted]: the
 * same tone over the blurred content under it, only the top bar, the dock and the rail) and sheets and dialogs
 * ([Solid], opaque). A blur costs a pass on every frame of the scroll, so it stays on these few bars.
 */
enum class GlassLevel {
    Sub,
    Standard,
    Elevated,
    Floating,
    Frosted,
    Solid,
}

/** Side of the hairline on [glassSurface]. [Start] and [End] follow the layout direction. */
enum class GlassEdge {
    Start,
    Top,
    End,
    Bottom;

    companion object {
        /** Every side. The line then follows the shape, brighter along the top. */
        val All: Set<GlassEdge> = entries.toSet()
    }
}

/**
 * Crystalline surface: translucent fill and a hairline that is brighter on top, like light falling from above.
 * [edges] chooses the sides that get the line. The shape clips the content too.
 */
@Composable
fun Modifier.glassSurface(
    level: GlassLevel = GlassLevel.Standard,
    shape: Shape = RoundedCornerShape(AppTheme.corner.large + AppTheme.corner.smallSpecial),
    edges: Set<GlassEdge> = GlassEdge.All
): Modifier {
    val appBorderWidth = AppTheme.borderWidth
    val appGlass = AppTheme.color.glass

    val fill = when (level) {
        GlassLevel.Sub -> appGlass.sub
        GlassLevel.Standard -> appGlass.standard
        GlassLevel.Elevated -> appGlass.elevated
        GlassLevel.Floating, GlassLevel.Frosted -> appGlass.dock
        GlassLevel.Solid -> appGlass.solid
    }
    // Every card and chip has a surface: the brush of its hairline is built once per scheme.
    val edgeBrush = remember(appGlass.edgeTop, appGlass.edge) {
        Brush.verticalGradient(listOf(appGlass.edgeTop, appGlass.edge))
    }

    return this
        .clip(shape)
        .applyIfComposable(level == GlassLevel.Frosted) { frostedBackdrop() }
        .background(fill)
        .glassEdges(
            edges = edges,
            shape = shape,
            width = appBorderWidth.small,
            brush = edgeBrush
        )
}

/**
 * The hairline on the chosen [edges]. The outline and the clip of the sides are built once per size
 * ([drawWithCache]): a bar over a scrolling list redraws on every frame and would build them again each time.
 */
private fun Modifier.glassEdges(
    edges: Set<GlassEdge>,
    shape: Shape,
    width: Dp,
    brush: Brush
): Modifier = when {
    edges == GlassEdge.All -> border(width = width, brush = brush, shape = shape)
    edges.isEmpty() -> this
    else -> drawWithCache {
        val stroke = width.toPx()
        val full = shape.createOutline(size, layoutDirection, this)
        val topReach = maxOf(stroke, full.cornerReach(top = true))
        val bottomReach = maxOf(stroke, full.cornerReach(top = false))
        val outline = shape.createOutline(
            Size((size.width - stroke).coerceAtLeast(0f), (size.height - stroke).coerceAtLeast(0f)),
            layoutDirection,
            this
        )
        val left = if (layoutDirection == LayoutDirection.Ltr) GlassEdge.Start else GlassEdge.End
        val right = if (layoutDirection == LayoutDirection.Ltr) GlassEdge.End else GlassEdge.Start
        val sideTop = if (GlassEdge.Top in edges) 0f else topReach.coerceAtMost(size.height)
        val sideBottom = if (GlassEdge.Bottom in edges) size.height else (size.height - stroke).coerceAtLeast(sideTop)
        val clip = Path().apply {
            if (GlassEdge.Top in edges) {
                addRect(Rect(0f, 0f, size.width, topReach.coerceAtMost(size.height)))
            }
            if (GlassEdge.Bottom in edges) {
                addRect(Rect(0f, (size.height - bottomReach).coerceAtLeast(0f), size.width, size.height))
            }
            if (sideBottom > sideTop) {
                if (left in edges) addRect(Rect(0f, sideTop, stroke, sideBottom))
                if (right in edges) addRect(Rect(size.width - stroke, sideTop, size.width, sideBottom))
            }
        }
        val strokeStyle = Stroke(width = stroke)

        onDrawWithContent {
            drawContent()

            clipPath(clip) {
                translate(left = stroke / 2f, top = stroke / 2f) {
                    drawOutline(outline = outline, brush = brush, style = strokeStyle)
                }
            }
        }
    }
}

private fun Outline.cornerReach(top: Boolean): Float {
    val rounded = this as? Outline.Rounded ?: return 0f
    val left = if (top) rounded.roundRect.topLeftCornerRadius else rounded.roundRect.bottomLeftCornerRadius
    val right = if (top) rounded.roundRect.topRightCornerRadius else rounded.roundRect.bottomRightCornerRadius
    return maxOf(left.y, right.y)
}

/** Card on a glass surface. Clickable when [onClick] is set. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Standard,
    corners: Dp = AppTheme.corner.large + AppTheme.corner.smallSpecial,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.largeSpecial),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glassSurface(level = level, shape = RoundedCornerShape(corners))
            .applyIfNotNull {
                onClick?.let { callback -> debouncedClickable(onClick = callback) }
            }
            .padding(paddingValues),
        content = content
    )
}