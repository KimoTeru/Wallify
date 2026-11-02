package org.kimoteru.wallify.core.designsystem.ui.animation.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

@Composable
fun animateColor(
    targetValue: Color,
    defaultValue: Color? = null,
    animationSpec: AnimationSpec<Color> = tween(AppTheme.durationAnim.small),
    visible: Boolean = true
) = animateColorAsState(
    targetValue = if (visible) targetValue else defaultValue ?: Color.Transparent,
    label = targetValue.toString(),
    animationSpec = animationSpec
).value

@Composable
fun animateDp(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small),
    visible: Boolean = true,
    finishedListener: ((Dp) -> Unit)? = null
) = animateDpAsState(
    targetValue = if (visible) targetValue else Dp.Hairline,
    label = targetValue.toString(),
    animationSpec = animationSpec,
    finishedListener = finishedListener
).value

@Composable
fun animateFloat(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Float) -> Unit)? = null
) = animateFloatAsState(
    targetValue = targetValue,
    label = targetValue.toString(),
    animationSpec = animationSpec,
    finishedListener = finishedListener
).value

@Composable
fun animateInt(
    targetValue: Int,
    animationSpec: AnimationSpec<Int> = tween(AppTheme.durationAnim.small)
) = animateIntAsState(
    targetValue = targetValue,
    label = targetValue.toString(),
    animationSpec = animationSpec
).value

@Composable
fun animateOffset(
    x: Dp = 0.dp,
    y: Dp = 0.dp,
    animationSpec: AnimationSpec<Offset> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Offset) -> Unit)? = null
): Offset {
    val density = LocalDensity.current
    val target = remember(x, y, density) {
        with(density) { Offset(x.toPx(), y.toPx()) }
    }
    return animateOffsetAsState(
        target,
        animationSpec = animationSpec,
        label = "animatedOffset",
        finishedListener = finishedListener
    ).value
}

@Composable
fun animateSize(
    target: Size,
    animationSpec: AnimationSpec<Size> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Size) -> Unit)? = null
): Size = animateSizeAsState(
    targetValue = target,
    animationSpec = animationSpec,
    label = "animatedOffset",
    finishedListener = finishedListener
).value

@Composable
fun animateScaleVisible(
    visible: Boolean,
    animationSpec: AnimationSpec<Float> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Float) -> Unit)? = null
) = animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    label = "animateScaleVisible",
    animationSpec = animationSpec,
    finishedListener = finishedListener
).value

@Composable
fun animateAlpha(
    targetValue: Float = 1f,
    visible: Boolean,
    animationSpec: AnimationSpec<Float> = tween(AppTheme.durationAnim.small)
) = animateFloatAsState(
    targetValue = if (visible) targetValue else 0f,
    animationSpec = animationSpec,
    label = targetValue.toString()
).value