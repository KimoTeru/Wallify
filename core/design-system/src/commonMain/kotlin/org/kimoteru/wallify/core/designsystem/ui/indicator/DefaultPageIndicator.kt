package org.kimoteru.wallify.core.designsystem.ui.indicator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha

@Composable
fun DefaultPageIndicator(
    modifier: Modifier = Modifier,
    pageCount: Int,
    currentPage: Int = 0,
    selectedColor: Color = AppTheme.color.basic.primary,
    defaultColor: Color = AppTheme.color.basic.primary.withAlpha(AppTheme.alpha.medium),
    defaultRadius: Dp = AppTheme.size.pageIndicator.defaultRadius,
    selectedLength: Dp = AppTheme.size.pageIndicator.selectedLength,
    space: Dp = AppTheme.padding.smallExtra,
    animationDurationInMillis: Int = AppTheme.durationAnim.small
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space, Alignment.CenterHorizontally)
    ) {
        for (i in 0 until pageCount) {
            PageIndicatorView(
                isSelected = i == currentPage,
                selectedColor = selectedColor,
                defaultColor = defaultColor,
                defaultRadius = defaultRadius,
                selectedLength = selectedLength,
                animationDurationInMillis = animationDurationInMillis
            )
        }
    }
}

@Composable
private fun PageIndicatorView(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    selectedColor: Color,
    defaultColor: Color,
    defaultRadius: Dp,
    selectedLength: Dp,
    animationDurationInMillis: Int
) {
    val color: Color by animateColorAsState(
        targetValue = if (isSelected) {
            selectedColor
        } else {
            defaultColor
        },
        animationSpec = tween(durationMillis = animationDurationInMillis)
    )

    val width: Dp by animateDpAsState(
        targetValue = if (isSelected) {
            selectedLength
        } else {
            defaultRadius
        },
        animationSpec = tween(durationMillis = animationDurationInMillis)
    )

    Canvas(
        modifier = modifier
            .size(
                width = width,
                height = defaultRadius
            )
    ) {
        drawRoundRect(
            color = color,
            topLeft = Offset.Zero,
            size = Size(
                width = width.toPx(),
                height = defaultRadius.toPx()
            ),
            cornerRadius = CornerRadius(
                x = defaultRadius.toPx(),
                y = defaultRadius.toPx()
            )
        )
    }
}