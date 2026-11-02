package org.kimoteru.wallify.core.designsystem.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull

@Composable
fun DefaultColumnCard(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.medium),
    backgroundColor: Color = AppTheme.color.basic.card.basic,
    shape: RoundedCornerShape? = null,
    corners: Dp? = null,
    elevation: Dp? = null,
    shadowColor: Color = DefaultShadowColor,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val appCorner = AppTheme.corner

    val finalShape = shape ?: RoundedCornerShape(corners ?: appCorner.mediumSpecial)

    Column(
        modifier = modifier
            .applyIfNotNull {
                elevation?.let { dp ->
                    shadow(elevation = dp, shape = finalShape, spotColor = shadowColor)
                }
            }
            .clip(finalShape)
            .applyIfNotNull {
                onClick?.let { onClick ->
                    debouncedClickable(onClick = onClick)
                }
            }
            .background(backgroundColor)
            .padding(paddingValues),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun DefaultRowCard(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.medium),
    backgroundColor: Color = AppTheme.color.basic.card.basic,
    shape: RoundedCornerShape? = null,
    corners: Dp? = null,
    elevation: Dp? = null,
    shadowColor: Color = DefaultShadowColor,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit = {}
) {
    val appCorner = AppTheme.corner

    val finalShape = shape ?: RoundedCornerShape(corners ?: appCorner.mediumSpecial)

    Row(
        modifier = modifier
            .applyIfNotNull {
                elevation?.let { dp ->
                    shadow(elevation = dp, shape = finalShape, spotColor = shadowColor)
                }
            }
            .clip(finalShape)
            .applyIfNotNull {
                onClick?.let { onClick ->
                    debouncedClickable(onClick = onClick)
                }
            }
            .background(backgroundColor)
            .padding(paddingValues),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}