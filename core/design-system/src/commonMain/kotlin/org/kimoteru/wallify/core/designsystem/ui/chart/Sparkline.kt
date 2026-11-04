package org.kimoteru.wallify.core.designsystem.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha

/** Line chart without axes for a short series, e.g. views per day; the area under the line fades out. */
@Composable
fun Sparkline(
    modifier: Modifier = Modifier,
    values: List<Long>,
    lineColor: Color = AppTheme.color.text.title
) {
    val appAlpha = AppTheme.alpha
    val appBorderWidth = AppTheme.borderWidth

    Box(modifier = modifier) {
        if (values.size < 2) return@Box

        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxValue = values.max()
            val minValue = values.min()
            val range = (maxValue - minValue).coerceAtLeast(1L).toFloat()
            val stepX = size.width / (values.size - 1)
            val strokeWidth = appBorderWidth.medium.toPx()
            val verticalInset = strokeWidth * 2

            val points = values.mapIndexed { index, value ->
                val fraction = (value - minValue) / range
                Offset(
                    x = stepX * index,
                    y = size.height - verticalInset - fraction * (size.height - verticalInset * 2)
                )
            }

            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }

            val area = Path().apply {
                addPath(line)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = area,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.withAlpha(appAlpha.small), Color.Transparent)
                )
            )

            drawPath(
                path = line,
                color = lineColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}