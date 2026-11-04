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
 * Frosted glass for a bar that floats over scrolling content (dock, rail, search): the content under it, blurred.
 * The blur lies on an opaque base color, otherwise the sharp content shows through the blurred copy and the bar
 * only looks translucent instead of matte. The surface puts its own tint over it.
 *
 * It sees the content marked with [blurSource] that is not a parent of the bar: the bar and the list are siblings.
 * Without a [BlurHost] it draws nothing, and the surface stays plain translucent.
 */
@Composable
fun Modifier.frostedBackdrop(level: BlurLevel = BlurLevel.Large): Modifier {
    val appBlur = AppTheme.blur
    val appColor = AppTheme.color

    val hazeState = LocalHazeState.current ?: return this

    val radius = when (level) {
        BlurLevel.Small -> appBlur.small
        BlurLevel.Medium -> appBlur.medium
        BlurLevel.Large -> appBlur.large
    }
    val base = appColor.basic.background

    val style = remember(radius, base) {
        HazeBlurStyle {
            blurRadius(radius)
            noiseFactor(0f)
            backgroundColor(base)
        }
    }

    return this.hazeBlur(
        input = HazeInput.Backdrop(hazeState),
        style = style
    )
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
    // The screen background lies under the blur source, so the blurred copy is translucent where the screen
    // draws nothing, and the sharp content shows through it as a ghost. An opaque base color behind the blur
    // closes that, the scrim then dims it evenly (dark on the day scheme too, or a white dialog would not stand out),
    // and no noise keeps it clean.
    val base = appColor.basic.background
    val tint = appColor.glass.scrim
    val scrim = appColor.basic.black.copy(alpha = appAlpha.largeSpecial)

    if (hazeState == null) {
        return this.background(scrim.copy(alpha = scrim.alpha * alpha))
    }

    val style = remember(radius, base, tint, scrim) {
        HazeBlurStyle {
            blurRadius(radius)
            noiseFactor(0f)
            backgroundColor(base)
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