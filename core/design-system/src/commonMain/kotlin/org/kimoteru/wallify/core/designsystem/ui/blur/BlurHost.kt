package org.kimoteru.wallify.core.designsystem.ui.blur

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Shared Haze state of the current blur scope, or null when there is no [BlurHost].
 * Without a state every blur modifier falls back to a plain translucent scrim, so previews and tests keep working.
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/** Z-order of blur sources. An effect inside a source only sees sources with a lower z-index. */
object BlurSourceZIndex {
    const val Root = 0f
    const val Screen = -1f
}

/**
 * Creates the blur scope for everything inside it. Place it once per screen (the root stack wraps every screen in
 * one), then mark the content that has to be blurred with [blurSource] and draw dialogs, sheets and similar overlays
 * next to that content, not inside it. See `docs/design/foundations.md`.
 */
@Composable
fun BlurHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(
            modifier = modifier,
            content = content
        )
    }
}

/**
 * Screen-level blur scope: [content] is the source and [overlay] (dialogs, sheets) is drawn above it,
 * so overlays blur the screen content. Its source has a lower z-index than the root one,
 * which lets the overlay see it even though the screen itself is inside the root source.
 */
@Composable
fun BlurLayout(
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blurSource(BlurSourceZIndex.Screen),
            content = content
        )

        overlay()
    }
}