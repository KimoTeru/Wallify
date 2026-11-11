package org.kimoteru.wallify.core.designsystem.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha

/**
 * Column chart of a short series, e.g. requests per day for a month, the oldest value first. Bars are as high as
 * their share of the largest value; [highlightIndex] (the last value by default, e.g. today) is drawn in
 * [highlightColor]. [maxLabel] is shown over the chart, [startLabel] and [endLabel] under its edges. The chart is one
 * element for accessibility: it reads [contentDescription] instead of the labels.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    values: List<Long>,
    startLabel: String,
    endLabel: String,
    maxLabel: String,
    contentDescription: String,
    highlightIndex: Int = values.lastIndex,
    barColor: Color = AppTheme.color.text.title.withAlpha(AppTheme.alpha.medium),
    highlightColor: Color = AppTheme.color.text.title,
    trackColor: Color = AppTheme.color.glass.divider,
    labelStyle: TextStyle = AppTheme.typography.h8.medium,
    labelColor: Color = AppTheme.color.text.subtitle
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    Column(
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(appPadding.smallExtra)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = maxLabel,
            style = labelStyle,
            color = labelColor,
            textAlign = TextAlign.End,
            maxLines = 1
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(appSize.chart.barChartHeight)
        ) {
            if (values.isEmpty()) return@Canvas

            val maxValue = values.max().coerceAtLeast(1L).toFloat()
            val gap = appSize.chart.barChartGap.toPx()
            val minBarHeight = appSize.chart.barChartMinBarHeight.toPx()
            val barWidth = ((size.width - gap * (values.size - 1)) / values.size).coerceAtLeast(1f)
            val radius = CornerRadius(barWidth / 2, barWidth / 2)

            values.forEachIndexed { index, value ->
                val barHeight = (value / maxValue * size.height).coerceAtLeast(minBarHeight)
                drawRoundRect(
                    color = when {
                        index == highlightIndex -> highlightColor
                        value > 0L -> barColor
                        else -> trackColor
                    },
                    topLeft = Offset(x = index * (barWidth + gap), y = size.height - barHeight),
                    size = Size(width = barWidth, height = barHeight),
                    cornerRadius = radius
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.weight(1f),
                text = startLabel,
                style = labelStyle,
                color = labelColor,
                maxLines = 1
            )

            Text(
                modifier = Modifier.weight(1f),
                text = endLabel,
                style = labelStyle,
                color = labelColor,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}