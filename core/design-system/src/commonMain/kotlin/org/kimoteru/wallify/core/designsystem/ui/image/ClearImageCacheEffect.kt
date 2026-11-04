package org.kimoteru.wallify.core.designsystem.ui.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext

/**
 * Empties the memory and disk cache of the image loader when [isCleared] becomes true. The loader is part of the UI
 * layer, so what the data layer wipes on signing out (`SessionDataCleaner`) does not reach it: the root calls this
 * when the sign-in screen is shown. It is a one-off at the moment the session ends.
 */
@Composable
fun ClearImageCacheEffect(isCleared: Boolean) {
    val platformContext = LocalPlatformContext.current

    LaunchedEffect(isCleared) {
        if (!isCleared) return@LaunchedEffect

        val imageLoader = SingletonImageLoader.get(platformContext)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}