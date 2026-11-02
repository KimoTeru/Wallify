package org.kimoteru.wallify.core.designsystem.ui.blur

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/** Strength of the blur, mapped to `AppTheme.blur`. */
enum class BlurLevel {
    Small,
    Medium,
    Large
}

/** Marks this content as the source that [blurBackdrop] surfaces blur. Does nothing without a [BlurHost]. */
@Composable
fun Modifier.blurSource(zIndex: Float = BlurSourceZIndex.Root): Modifier {
    val hazeState = LocalHazeState.current ?: return this
    return hazeSource(state = hazeState, zIndex = zIndex)
}

/**
 * Draws a blurred copy of the content behind this surface instead of a dim scrim.
 * [alpha] fades the effect in and out (0f is invisible, 1f is fully applied), so dialogs and sheets can animate it.
 *
 * Without a [BlurHost], or where the platform cannot blur, it falls back to a translucent dark scrim.
 */
@Composable
fun Modifier.blurBackdrop(
    level: BlurLevel = BlurLevel.Medium,
    alpha: Float = 1f
): Modifier {
    val appAlpha = AppTheme.alpha
    val appBlur = AppTheme.blur
    val appColor = AppTheme.color

    val currentAlpha = rememberUpdatedState(alpha)
    val hazeState = LocalHazeState.current

    val radius = when (level) {
        BlurLevel.Small -> appBlur.small
        BlurLevel.Medium -> appBlur.medium
        BlurLevel.Large -> appBlur.large
    }
    val tint = appColor.basic.dark.copy(alpha = appAlpha.small)
    val scrim = appColor.basic.black.copy(alpha = appAlpha.largeSpecial)

    if (hazeState == null) {
        return this.background(scrim.copy(alpha = scrim.alpha * alpha))
    }

    val style = remember(radius, tint, scrim) {
        HazeBlurStyle {
            blurRadius(radius)
            colorEffects(listOf(HazeColorEffect.tint(tint)))
            fallbackColorEffect(HazeColorEffect.tint(scrim))
            alpha(currentAlpha.value)
        }
    }

    return this.hazeBlur(
        input = HazeInput.Backdrop(hazeState),
        style = style
    )
}