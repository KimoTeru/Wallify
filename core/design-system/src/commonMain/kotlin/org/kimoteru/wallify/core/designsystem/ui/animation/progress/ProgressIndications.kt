package org.kimoteru.wallify.core.designsystem.ui.animation.progress

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

@Composable
fun DefaultCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.color.basic.primary,
    strokeWidth: Dp = AppTheme.borderWidth.medium,
    trackColor: Color = Color.Transparent
) {
    CircularProgressIndicator(
        strokeWidth = strokeWidth,
        modifier = modifier,
        color = color,
        trackColor = trackColor
    )
}