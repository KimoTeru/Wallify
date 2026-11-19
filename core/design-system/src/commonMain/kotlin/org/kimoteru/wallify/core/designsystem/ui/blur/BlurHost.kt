package org.kimoteru.wallify.core.designsystem.ui.blur

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalOverlayHost
import org.kimoteru.wallify.core.designsystem.ui.glass.OverlayHost

/**
 * Shared Haze state of the current blur scope, or null when there is no [BlurHost].
 * Without a state every blur modifier falls back to a plain translucent scrim, so previews and tests keep working.
 */
internal val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/**
 * The blur scope of one root screen. Inside it the content of the screen is marked with [blurSource]
 * (`FloatingBarScreen` does it for its list), the frosted bars (top bar, dock, rail) are siblings of that content,
 * and the overlays of the screen (`ScreenOverlay`: sheets and dialogs) are drawn here, after everything the screen
 * owns. The host itself is no source: an effect inside a source cannot sample it. See `docs/design/foundations.md`
 * § Блюр.
 */
@Composable
fun BlurHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = rememberHazeState()
    val overlayHost = remember { OverlayHost() }

    CompositionLocalProvider(
        LocalHazeState provides hazeState,
        LocalOverlayHost provides overlayHost
    ) {
        Box(modifier = modifier) {
            content()

            overlayHost.content?.invoke(this)
        }
    }
}