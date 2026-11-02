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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

private const val SHIMMER_DURATION_MILLIS = 1200
private const val SHIMMER_HIGHLIGHT_ALPHA = 0.35f

/**
 * Animated loading placeholder. Pure Compose Multiplatform, no external placeholder library.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.color.basic.dark.copy(alpha = AppTheme.alpha.small),
    highlightColor: Color = AppTheme.color.basic.white.copy(alpha = SHIMMER_HIGHLIGHT_ALPHA),
    shape: Shape = RoundedCornerShape(AppTheme.corner.mediumSpecial),
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .shimmer(highlightColor),
        content = { content?.invoke(this) }
    )
}

/** Draws a moving highlight band over the content. */
fun Modifier.shimmer(
    highlightColor: Color
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    drawWithContent {
        drawContent()
        val width = size.width
        val bandWidth = width * 0.6f
        val startX = -bandWidth + (width + bandWidth) * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, highlightColor, Color.Transparent),
                start = Offset(startX, 0f),
                end = Offset(startX + bandWidth, size.height)
            )
        )
    }
}