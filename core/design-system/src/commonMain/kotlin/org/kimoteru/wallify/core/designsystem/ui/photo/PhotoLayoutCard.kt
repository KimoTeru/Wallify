package org.kimoteru.wallify.core.designsystem.ui.photo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateColor
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable

private const val GAP_FRACTION = 0.07f
private const val CORNER_FRACTION = 0.06f
private const val THIRD = 1f / 3f

/**
 * Choice of a photo layout shown as a small picture of that layout, so the user sees what the names mean.
 * The chosen card is outlined and brighter.
 */
@Composable
fun PhotoLayoutCard(
    modifier: Modifier = Modifier,
    mode: PhotoGridMode,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appBorderWidth = AppTheme.borderWidth
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appRatio = AppTheme.ratio
    val appTypography = AppTheme.typography

    val shape = RoundedCornerShape(appCorner.medium)
    val background = animateColor(if (selected) appColor.glass.elevated else appColor.glass.sub)
    val borderColor = animateColor(if (selected) appColor.text.title else appColor.glass.edge)
    val contentColor = animateColor(
        if (selected) appColor.text.title else appColor.text.subtitle.withAlpha(appAlpha.largeExtra)
    )

    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(if (selected) appBorderWidth.medium else appBorderWidth.small, borderColor, shape)
            .debouncedClickable(onClick = onClick)
            .padding(appPadding.mediumSpecial),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial)
    ) {
        PhotoLayoutPreview(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(appRatio.image.square),
            mode = mode,
            color = contentColor
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = label,
            style = appTypography.h7.semiBold,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Columns of cards, every card given as its share of the column height; the shares of a column sum to 1. */
@Composable
private fun PhotoLayoutPreview(
    modifier: Modifier = Modifier,
    mode: PhotoGridMode,
    color: Color
) {
    val columns = when (mode) {
        PhotoGridMode.Masonry -> listOf(listOf(0.6f, 0.4f), listOf(0.4f, 0.6f))
        PhotoGridMode.Grid -> listOf(listOf(THIRD, THIRD, THIRD), listOf(THIRD, THIRD, THIRD))
        PhotoGridMode.List -> listOf(listOf(0.5f, 0.5f))
    }

    Canvas(modifier = modifier) {
        drawColumns(columns = columns, color = color)
    }
}

private fun DrawScope.drawColumns(columns: List<List<Float>>, color: Color) {
    val gap = size.width * GAP_FRACTION
    val columnWidth = (size.width - gap * (columns.size - 1)) / columns.size
    val corner = CornerRadius(size.width * CORNER_FRACTION)

    columns.forEachIndexed { columnIndex, shares ->
        val usableHeight = size.height - gap * (shares.size - 1)
        var top = 0f

        shares.forEach { share ->
            val height = usableHeight * share

            drawRoundRect(
                color = color,
                topLeft = Offset(x = (columnWidth + gap) * columnIndex, y = top),
                size = Size(width = columnWidth, height = height),
                cornerRadius = corner
            )

            top += height + gap
        }
    }
}