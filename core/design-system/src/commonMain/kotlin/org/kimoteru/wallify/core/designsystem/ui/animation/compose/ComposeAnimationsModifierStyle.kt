package org.kimoteru.wallify.core.designsystem.ui.animation.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

@Composable
fun Modifier.animatedBackground(
    color: Color,
    visible: Boolean = true,
    animationSpec: AnimationSpec<Color> = tween(AppTheme.durationAnim.small)
): Modifier {
    val animatedColor by animateColorAsState(
        targetValue = if (visible) color else Color.Transparent,
        animationSpec = animationSpec,
        label = color.toString()
    )
    return this.background(animatedColor)
}

@Composable
fun Modifier.animatedAlpha(
    alpha: Float,
    visible: Boolean = true,
    animationSpec: AnimationSpec<Float> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Float) -> Unit)? = null
): Modifier {
    val animatedAlpha =
        animateFloat(
            targetValue = if (visible) alpha else 0f,
            animationSpec = animationSpec,
            finishedListener = finishedListener
        )
    return this.alpha(animatedAlpha)
}

@Composable
fun Modifier.animatedAlpha(
    visible: Boolean = true,
    animationSpec: AnimationSpec<Float> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Float) -> Unit)? = null
): Modifier {
    val appAlpha = AppTheme.alpha

    val animatedAlpha =
        animateFloat(
            targetValue = if (visible) appAlpha.full else 0f,
            animationSpec = animationSpec,
            finishedListener = finishedListener
        )
    return this.alpha(animatedAlpha)
}

@Composable
fun Modifier.animatedPadding(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
    visible: Boolean = true,
    animationSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small)
): Modifier {
    val animatedHorizontal =
        animateDp(
            targetValue = horizontal,
            visible = visible,
            animationSpec = animationSpec
        )
    val animatedVertical =
        animateDp(
            targetValue = vertical,
            visible = visible,
            animationSpec = animationSpec
        )
    return this.padding(horizontal = animatedHorizontal, vertical = animatedVertical)
}

@Composable
fun Modifier.animatedPadding(
    paddingValues: PaddingValues = PaddingValues(),
    visible: Boolean = true,
    animationSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small)
): Modifier {
    val layoutDirection = LocalLayoutDirection.current

    return animatedPadding(
        start = paddingValues.calculateStartPadding(layoutDirection),
        top = paddingValues.calculateTopPadding(),
        end = paddingValues.calculateEndPadding(layoutDirection),
        bottom = paddingValues.calculateBottomPadding(),
        visible = visible,
        animationSpec = animationSpec
    )
}

@Composable
fun Modifier.animatedPadding(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
    visible: Boolean = true,
    animationSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small)
): Modifier {
    val animatedStart = animateDp(
        targetValue = start,
        visible = visible,
        animationSpec = animationSpec
    )
    val animatedTop = animateDp(
        targetValue = top,
        visible = visible,
        animationSpec = animationSpec
    )
    val animatedEnd = animateDp(
        targetValue = end,
        visible = visible,
        animationSpec = animationSpec
    )
    val animatedBottom = animateDp(
        targetValue = bottom,
        visible = visible,
        animationSpec = animationSpec
    )

    return this.padding(
        start = animatedStart,
        top = animatedTop,
        end = animatedEnd,
        bottom = animatedBottom
    )
}

@Composable
fun Modifier.animatedBorder(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape,
    visible: Boolean = true,
    widthSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small),
    colorSpec: AnimationSpec<Color> = tween(AppTheme.durationAnim.small)
): Modifier {
    val animatedWidth = animateDp(
        targetValue = width,
        visible = visible,
        animationSpec = widthSpec
    )
    val animatedColor =
        animateColor(
            targetValue = if (visible) color else Color.Transparent,
            animationSpec = colorSpec
        )

    return this.border(width = animatedWidth, color = animatedColor, shape = shape)
}