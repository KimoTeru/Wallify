package org.kimoteru.wallify.core.designsystem.ui.indicator

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/**
 * Spinning arc that turns into a check mark when [isCompleted]. The rotation runs on every frame, so the arc, the
 * check, the brush and the stroke are built once per size ([drawWithCache]) and the animated values are read only
 * while drawing.
 */
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

    val arcColor = appColor.text.subtitle
    val checkColor = appColor.basic.primary
    val strokeWidth = appSize.progressIndicator.strokeWidthSmall

    Spacer(
        modifier = modifier
            .size(appSize.progressIndicator.sizeMedium)
            .drawWithCache {
                val gradientBrush = Brush.sweepGradient(colors = listOf(Color.Transparent, arcColor, arcColor, Color.Transparent))
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                val radius = size.minDimension / 2 - stroke.width
                val center = Offset(size.width / 2, size.height / 2)
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)
                val check = Path().apply {
                    moveTo(center.x - radius * 0.6f, center.y)
                    lineTo(center.x - radius * 0.15f, center.y + radius * 0.4f)
                    lineTo(center.x + radius * 0.6f, center.y - radius * 0.5f)
                }

                onDrawBehind {
                    if (progressAlpha > 0f) {
                        rotate(rotation, pivot = center) {
                            drawArc(
                                brush = gradientBrush,
                                startAngle = 0f,
                                sweepAngle = 240f,
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = stroke
                            )
                        }
                    }

                    if (checkAlpha > 0f) {
                        drawPath(
                            path = check,
                            color = checkColor,
                            style = stroke,
                            alpha = checkAlpha
                        )
                    }
                }
            }
    )
}