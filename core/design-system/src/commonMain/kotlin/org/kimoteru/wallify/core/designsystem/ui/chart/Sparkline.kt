package org.kimoteru.wallify.core.designsystem.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha

/**
 * Line chart without axes for a short series, e.g. views per day; the area under the line fades out. The chart lies
 * on scrolling screens and is drawn again with them: the line, the area and the brush are built once per size and
 * series ([drawWithCache]), not on every frame.
 */
@Composable
fun Sparkline(
    modifier: Modifier = Modifier,
    values: List<Long>,
    lineColor: Color = AppTheme.color.text.title
) {
    val appAlpha = AppTheme.alpha
    val appBorderWidth = AppTheme.borderWidth

    val areaAlpha = appAlpha.small
    val lineWidth = appBorderWidth.medium

    Box(modifier = modifier) {
        if (values.size < 2) return@Box

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val maxValue = values.max()
                    val minValue = values.min()
                    val range = (maxValue - minValue).coerceAtLeast(1L).toFloat()
                    val stepX = size.width / (values.size - 1)
                    val strokeWidth = lineWidth.toPx()
                    val verticalInset = strokeWidth * 2

                    val line = Path()
                    values.forEachIndexed { index, value ->
                        val fraction = (value - minValue) / range
                        val x = stepX * index
                        val y = size.height - verticalInset - fraction * (size.height - verticalInset * 2)
                        if (index == 0) line.moveTo(x, y) else line.lineTo(x, y)
                    }

                    val area = Path().apply {
                        addPath(line)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    val areaBrush = Brush.verticalGradient(
                        colors = listOf(lineColor.withAlpha(areaAlpha), Color.Transparent)
                    )
                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

                    onDrawBehind {
                        drawPath(path = area, brush = areaBrush)
                        drawPath(path = line, color = lineColor, style = stroke)
                    }
                }
        )
    }
}