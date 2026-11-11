package org.kimoteru.wallify.core.designsystem.ui.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

private const val SHIMMER_DURATION_MILLIS = 1200
private const val SHIMMER_HIGHLIGHT_ALPHA = 0.35f
private const val SHIMMER_BAND_FRACTION = 0.6f

/**
 * Animated loading placeholder. Pure Compose Multiplatform, no external placeholder library.
 *
 * The progress of the highlight is read only in the draw phase: a running shimmer redraws the box and composes
 * nothing again.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.color.basic.dark.copy(alpha = AppTheme.alpha.small),
    highlightColor: Color = AppTheme.color.basic.white.copy(alpha = SHIMMER_HIGHLIGHT_ALPHA),
    shape: Shape = RoundedCornerShape(AppTheme.corner.mediumSpecial),
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .shimmer(highlightColor = highlightColor, progress = progress),
        content = { content?.invoke(this) }
    )
}

/**
 * Draws a moving highlight band over the content. The band is built once per size and color ([drawWithCache]) and
 * only moved on every frame, so the animation allocates nothing.
 */
private fun Modifier.shimmer(
    highlightColor: Color,
    progress: State<Float>
): Modifier = drawWithCache {
    val bandWidth = size.width * SHIMMER_BAND_FRACTION
    val band = Brush.linearGradient(
        colors = listOf(Color.Transparent, highlightColor, Color.Transparent),
        start = Offset.Zero,
        end = Offset(bandWidth, size.height)
    )

    onDrawWithContent {
        drawContent()
        val startX = -bandWidth + (size.width + bandWidth) * progress.value
        translate(left = startX) {
            drawRect(brush = band, topLeft = Offset.Zero, size = size.copy(width = bandWidth))
        }
    }
}