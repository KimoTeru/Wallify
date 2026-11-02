package org.kimoteru.wallify.core.designsystem.ui.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withColor
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateFloat
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull

@Composable
fun DefaultButtonContainer(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.medium),
    backgroundColor: Color? = null,
    borderWidth: Dp? = null,
    borderColor: Color? = null,
    corners: Dp = AppTheme.corner.large,
    enabled: Boolean = true,
    horizontalArrangement: Arrangement.HorizontalOrVertical = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val appAlpha = AppTheme.alpha
    val alpha =
        animateFloat(targetValue = if (enabled) appAlpha.full else appAlpha.largeSpecial)

    Row(
        modifier = modifier
            .alpha(alpha)
            .clip(shape = RoundedCornerShape(corners))
            .applyIfNotNull {
                backgroundColor?.let {
                    background(color = it)
                }
            }
            .applyIfNotNull {
                borderWidth?.let { borderWidth ->
                    borderColor?.let {
                        border(
                            borderWidth,
                            it,
                            RoundedCornerShape(corners)
                        )
                    }
                }
            }
            .debouncedClickable(onClick = onClick, enabled = enabled)
            .padding(paddingValues),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        content()
    }
}

@Composable
fun DefaultTextButton(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = AppTheme.color.basic.button.primary,
    textStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.text.reversed),
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.medium),
    enabled: Boolean = true,
    corners: Dp = AppTheme.corner.large,
    onClick: () -> Unit
) {
    DefaultButtonContainer(
        modifier = modifier,
        backgroundColor = backgroundColor,
        paddingValues = paddingValues,
        onClick = onClick,
        corners = corners,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DefaultTextIconButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: @Composable (() -> Unit)? = null,
    backgroundColor: Color = AppTheme.color.basic.button.primary,
    textStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.text.reversed),
    textAlign: TextAlign = TextAlign.Center,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.medium),
    enabled: Boolean = true,
    corners: Dp = AppTheme.corner.large,
    onClick: () -> Unit
) {
    val appPadding = AppTheme.padding

    DefaultButtonContainer(
        modifier = modifier,
        backgroundColor = backgroundColor,
        paddingValues = paddingValues,
        onClick = onClick,
        corners = corners,
        enabled = enabled
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(appPadding.small))
        }

        Text(
            text = text,
            style = textStyle,
            textAlign = textAlign
        )
    }
}

@Composable
fun DefaultOutlineButton(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.basic.button.primary),
    borderColor: Color = AppTheme.color.basic.primary,
    borderWidth: Dp = AppTheme.borderWidth.medium,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.medium),
    enabled: Boolean = true,
    corners: Dp = AppTheme.corner.large,
    onClick: () -> Unit
) {
    DefaultButtonContainer(
        modifier = modifier,
        borderColor = borderColor,
        borderWidth = borderWidth,
        paddingValues = paddingValues,
        enabled = enabled,
        corners = corners,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DefaultOutlineButton(
    modifier: Modifier = Modifier,
    text: String,
    leftIcon: @Composable (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(
        AppTheme.color.basic.button.primary),
    borderColor: Color = AppTheme.color.basic.primary,
    borderWidth: Dp = AppTheme.borderWidth.medium,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.medium),
    enabled: Boolean = true,
    corners: Dp = AppTheme.corner.large,
    onClick: () -> Unit
) {
    val appPadding = AppTheme.padding

    DefaultButtonContainer(
        modifier = modifier,
        borderColor = borderColor,
        borderWidth = borderWidth,
        paddingValues = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(appPadding.small),
        enabled = enabled,
        corners = corners,
        onClick = onClick
    ) {
        leftIcon?.invoke()

        Text(
            modifier = Modifier,
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
        )

        rightIcon?.invoke()
    }
}