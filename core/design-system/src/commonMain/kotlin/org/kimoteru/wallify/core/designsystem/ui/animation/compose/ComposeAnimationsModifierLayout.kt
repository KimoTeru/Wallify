package org.kimoteru.wallify.core.designsystem.ui.animation.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull
import kotlin.math.roundToInt

@Composable
fun Modifier.animatedHeight(
    visible: Boolean,
    animationSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small)
): Modifier = composed {
    val density = LocalDensity.current

    var measuredHeight by remember { mutableStateOf<Dp?>(null) }
    var animateNormally by remember { mutableStateOf(!visible) }

    val targetHeight = if (visible) measuredHeight ?: Dp.Hairline else Dp.Hairline
    val currentAnimationSpec = if (!animateNormally && visible) snap() else animationSpec

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        label = "AnimatedHeight",
        animationSpec = currentAnimationSpec
    )

    LaunchedEffect(visible) {
        if (!animateNormally && !visible) {
            animateNormally = true
        }
    }

    this
        .onGloballyPositioned {
            if (measuredHeight == null) {
                with(density) {
                    measuredHeight = it.size.height.toDp()
                }
            }
        }
        .applyIfNotNull {
            measuredHeight?.let { height(animatedHeight) }
        }
}

@Composable
fun Modifier.animatedWidth(
    visible: Boolean,
    animationSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small)
): Modifier = composed {
    val density = LocalDensity.current

    var measuredWidth by remember { mutableStateOf<Dp?>(null) }
    val targetWidth = if (visible) measuredWidth ?: Dp.Hairline else Dp.Hairline

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        label = "AnimatedWidth",
        animationSpec = animationSpec
    )

    this
        .onGloballyPositioned {
            if (measuredWidth == null) {
                with(density) {
                    measuredWidth = it.size.width.toDp()
                }
            }
        }
        .applyIfNotNull {
            measuredWidth?.let { width(animatedWidth) }
        }
}

@Composable
fun Modifier.animatedOffset(
    x: Dp = 0.dp,
    y: Dp = 0.dp,
    animationSpec: AnimationSpec<Offset> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Offset) -> Unit)? = null
): Modifier = composed {
    val animOffset = animateOffset(
        x = x,
        y = y,
        animationSpec = animationSpec,
        finishedListener = finishedListener
    )

    this.offset { IntOffset(animOffset.x.roundToInt(), animOffset.y.roundToInt()) }
}

@Composable
fun Modifier.animatedSize(
    visible: Boolean,
    animationSpec: AnimationSpec<Size> = tween(AppTheme.durationAnim.small)
): Modifier = composed {
    val density = LocalDensity.current

    var initialized by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(Size.Zero) }

    val animatedSize = animateSize(
        target = if (visible) size else Size.Zero,
        animationSpec = animationSpec
    )

    this
        .onGloballyPositioned {
            if (!initialized) {
                with(density) {
                    size = it.size.toSize()
                    initialized = true
                }
            }
        }
        .then(
            if (initialized) Modifier.size(with(density) { animatedSize.toDpSize() })
            else Modifier
        )
}

@Composable
fun Modifier.animatedScale(
    visible: Boolean,
    animationSpec: AnimationSpec<Float> = tween(AppTheme.durationAnim.small)
): Modifier = composed {
    val scale = animateFloat(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animationSpec
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .layout { measurable, constraints ->
            if (scale == 0f) {
                layout(0, 0) {}
            } else {
                val placeable = measurable.measure(constraints)
                val width = (placeable.width * scale).toInt()
                val height = (placeable.height * scale).toInt()
                layout(width, height) {
                    placeable.placeRelative(
                        x = ((width - placeable.width) / 2f).toInt(),
                        y = ((height - placeable.height) / 2f).toInt()
                    )
                }
            }
        }
}

@Composable
fun Modifier.animatedCollapsibleHeight(
    visible: Boolean,
    animationSpec: AnimationSpec<Dp> = tween(AppTheme.durationAnim.small),
    finishedListener: ((Dp) -> Unit)? = null
): Modifier = composed {
    val density = LocalDensity.current

    var measuredHeight by remember { mutableStateOf<Dp?>(null) }
    var animateNormally by remember { mutableStateOf(!visible) }

    val targetHeight = if (visible) measuredHeight ?: Dp.Hairline else Dp.Hairline
    val currentAnimationSpec = if (!animateNormally && visible) snap() else animationSpec

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        label = "AnimatedCollapsibleHeight",
        animationSpec = currentAnimationSpec,
        finishedListener = finishedListener
    )

    LaunchedEffect(visible) {
        if (!animateNormally && !visible) {
            animateNormally = true
        }
    }

    this
        .graphicsLayer {
            clip = true
        }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minHeight = 0))
            val placeableHeight = with(density) { placeable.height.toDp() }

            if (measuredHeight != placeableHeight) {
                measuredHeight = placeableHeight
            }

            val animatedHeightPx = animatedHeight.roundToPx().coerceAtMost(placeable.height)

            layout(placeable.width, animatedHeightPx) {
                if (animatedHeightPx > 0) {
                    placeable.placeRelative(0, 0)
                }
            }
        }
}