package org.kimoteru.wallify.core.designsystem.ui.animation.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.kimoteru.wallify.core.common.extensions.half
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

@Composable
fun AlphaAnimated(
    modifier: Modifier = Modifier,
    visible: Boolean,
    duration: Int = AppTheme.durationAnim.small,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(duration)) + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut(animationSpec = tween(duration)) + slideOutVertically(targetOffsetY = { -it }),
        content = content
    )
}

@Composable
fun HidingAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = AppTheme.durationAnim.small,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(duration)),
        exit = fadeOut(animationSpec = tween(duration)),
        content = content
    )
}

@Composable
fun <T> HidingAnimatedContent(
    targetState: T,
    modifier: Modifier = Modifier,
    duration: Int = AppTheme.durationAnim.small,
    content: @Composable AnimatedContentScope.(targetState: T) -> Unit
) {
    AnimatedContent(
        modifier = modifier,
        targetState = targetState,
        label = targetState.toString(),
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = duration
                )
            ).togetherWith(fadeOut(animationSpec = tween(duration)))
        },
        content = content
    )
}

@Composable
fun HidingHorizontalAnimated(
    modifier: Modifier = Modifier,
    visible: Boolean,
    duration: Int = AppTheme.durationAnim.small,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(duration)) +
                slideInHorizontally(animationSpec = tween(duration)) { fullWidth -> fullWidth },
        exit = fadeOut(animationSpec = tween(duration)) +
                slideOutHorizontally(animationSpec = tween(duration)) { fullWidth -> fullWidth },
        content = content
    )
}

@Composable
fun HidingVerticalDownAnimated(
    modifier: Modifier = Modifier,
    visible: Boolean,
    duration: Int = AppTheme.durationAnim.small,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(duration))
                + expandVertically(animationSpec = tween(duration)),
        exit = fadeOut(animationSpec = tween(duration))
                + shrinkVertically(animationSpec = tween(duration)),
        content = content
    )
}

@Composable
fun HidingVerticalTopAnimated(
    modifier: Modifier = Modifier,
    visible: Boolean,
    duration: Int = AppTheme.durationAnim.small,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(duration))
                + slideInVertically(animationSpec = tween(duration)) {
            it.half
        },
        exit = fadeOut(animationSpec = tween(duration))
                + slideOutVertically(animationSpec = tween(duration)) {
            it.half
        },
        content = content
    )
}