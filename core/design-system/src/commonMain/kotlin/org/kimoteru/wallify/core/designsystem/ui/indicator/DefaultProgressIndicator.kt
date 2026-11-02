package org.kimoteru.wallify.core.designsystem.ui.indicator

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

@Composable
fun DefaultProgressIndicator(
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    val appSize = AppTheme.size
    val appColor = AppTheme.color
    val appDurationAnim = AppTheme.durationAnim

    val rotation by rememberInfiniteTransition()
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(appDurationAnim.large, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

    val transition = updateTransition(targetState = isCompleted)

    val progressAlpha by transition.animateFloat(
        transitionSpec = {
            tween(appDurationAnim.small)
        }
    ) {
        if (it) 0f else 1f
    }

    val checkAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = appDurationAnim.small, delayMillis = appDurationAnim.small)
        }
    ) {
        if (it) 1f else 0f
    }

    val gradientBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            appColor.text.subtitle,
            appColor.text.subtitle,
            Color.Transparent
        )
    )

    Canvas(modifier = modifier.size(appSize.progressIndicator.sizeMedium)) {
        val radius = size.minDimension / 2 - appSize.progressIndicator.strokeWidthSmall.toPx()
        val center = Offset(size.width / 2, size.height / 2)

        if (progressAlpha > 0f) {
            rotate(rotation, pivot = center) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = 0f,
                    sweepAngle = 240f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = appSize.progressIndicator.strokeWidthSmall.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        if (checkAlpha > 0f) {
            drawPath(
                path = Path().apply {
                    moveTo(center.x - radius * 0.6f, center.y)
                    lineTo(center.x - radius * 0.15f, center.y + radius * 0.4f)
                    lineTo(center.x + radius * 0.6f, center.y - radius * 0.5f)
                },
                color = appColor.basic.primary,
                style = Stroke(
                    width = appSize.progressIndicator.strokeWidthSmall.toPx(),
                    cap = StrokeCap.Round
                ),
                alpha = checkAlpha
            )
        }
    }
}