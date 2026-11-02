package org.kimoteru.wallify.core.designsystem.ui.divider

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

@Composable
fun DefaultHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = AppTheme.size.divider.thickness,
    color: Color = AppTheme.color.icons.nav
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

@Composable
fun DefaultVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = AppTheme.size.divider.thickness,
    color: Color = AppTheme.color.icons.nav
) {
    VerticalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

@Composable
fun DashedHorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.color.icons.nav,
    thickness: Dp = AppTheme.size.divider.thickness,
    dashLength: Dp = AppTheme.size.divider.dashLength,
    gapLength: Dp = AppTheme.size.divider.gapLength
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
    ) {
        val canvasWidth = size.width
        val dashPx = dashLength.toPx()
        val gapPx = gapLength.toPx()
        var startX = 0f

        while (startX < canvasWidth) {
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(x = startX, y = size.height / 2),
                end = androidx.compose.ui.geometry.Offset(
                    x = (startX + dashPx).coerceAtMost(
                        canvasWidth
                    ), y = size.height / 2
                ),
                strokeWidth = thickness.toPx()
            )
            startX += dashPx + gapPx
        }
    }
}