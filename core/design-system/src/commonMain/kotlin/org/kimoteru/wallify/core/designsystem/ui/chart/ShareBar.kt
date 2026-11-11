package org.kimoteru.wallify.core.designsystem.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/** Horizontal bar filled by [fraction] (0…1) of its width, e.g. the share of one item among all of them. */
@Composable
fun ShareBar(
    modifier: Modifier = Modifier,
    fraction: Float,
    fillColor: Color = AppTheme.color.text.title,
    trackColor: Color = AppTheme.color.glass.divider
) {
    val appSize = AppTheme.size

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(appSize.chart.shareBarHeight)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(fillColor)
        )
    }
}