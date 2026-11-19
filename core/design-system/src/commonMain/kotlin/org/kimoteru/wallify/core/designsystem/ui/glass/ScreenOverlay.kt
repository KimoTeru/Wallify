package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/** Place for the content of [ScreenOverlay]; the root screen (`BlurHost`) draws it above everything it owns. */
@Stable
class OverlayHost {
    var content by mutableStateOf<(@Composable BoxScope.() -> Unit)?>(null)
}

val LocalOverlayHost = staticCompositionLocalOf<OverlayHost?> { null }

/**
 * Draws [content] (a sheet or a dialog, see [GlassOverlay]) above the whole root screen, the navigation dock and
 * rail included, instead of inside the screen, where they would cover it. There it is next to the blur source, so
 * its backdrop blurs the screen. The overlay lives while this call stays in the composition.
 * Without a host (previews) the content is drawn in place.
 */
@Composable
fun ScreenOverlay(content: @Composable BoxScope.() -> Unit) {
    val host = LocalOverlayHost.current

    if (host == null) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
        return
    }

    SideEffect { host.content = content }

    DisposableEffect(host) {
        onDispose { host.content = null }
    }
}