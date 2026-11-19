package org.kimoteru.wallify.core.designsystem.ui.blur

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeInputSharing
import dev.chrisbanes.haze.HazePerformanceMode
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

/**
 * Marks the content of a screen that the frosted bars and the overlays blur: the list or the column under the top
 * bar, never the bars themselves. One per screen, and the bars and overlays are its siblings, not its children:
 * an effect cannot sample a source it lies in. Does nothing without a [BlurHost].
 *
 * The top bar and the dock sample the same scrolling content on every frame; with
 * [HazeInputSharing.Auto] Android renders it once for both instead of once per bar. Other platforms do not reuse the
 * render, and there `Auto` stays the default `Never`. A source records nothing while no effect samples it.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
fun Modifier.blurSource(): Modifier {
    val hazeState = LocalHazeState.current ?: return this
    return hazeSource(state = hazeState, inputSharing = HazeInputSharing.Auto)
}

/**
 * Frosted glass of a bar over scrolling content (dock, rail, top bar): the content under it, blurred, on an opaque
 * base color, otherwise the sharp content shows through the blurred copy and the bar only looks translucent. The
 * surface puts its own tint over it. Without a [BlurHost] it draws nothing, and the surface stays translucent.
 */
@Composable
internal fun Modifier.frostedBackdrop(level: BlurLevel = BlurLevel.Large): Modifier {
    val appBlur = AppTheme.blur
    val appColor = AppTheme.color

    val hazeState = LocalHazeState.current ?: return this

    val radius = level.radius(appBlur.small, appBlur.medium, appBlur.large)
    val base = appColor.basic.background

    val style = remember(radius, base) {
        HazeBlurStyle {
            blurRadius(radius)
            noiseFactor(0f)
            backgroundColor(base)
        }
    }

    // A bar redraws on every frame of the scroll; at its radius the lowest input quality looks the same and costs
    // much less (Haze performance guide).
    return this.hazeBlur(
        input = HazeInput.Backdrop(hazeState),
        style = style,
        performanceMode = HazePerformanceMode.Performance
    )
}

/**
 * Draws a blurred copy of the screen behind this surface instead of a dim scrim: the backdrop of a sheet or a
 * dialog. [alpha] fades the effect in and out (0f is invisible, 1f is fully applied), so dialogs and sheets can
 * animate it.
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

    val radius = level.radius(appBlur.small, appBlur.medium, appBlur.large)
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

    // The alpha is read through the state on every frame of the fade, so the style is not built again for it.
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

private fun <T> BlurLevel.radius(
    small: T,
    medium: T,
    large: T,
): T = when (this) {
    BlurLevel.Small -> small
    BlurLevel.Medium -> medium
    BlurLevel.Large -> large
}