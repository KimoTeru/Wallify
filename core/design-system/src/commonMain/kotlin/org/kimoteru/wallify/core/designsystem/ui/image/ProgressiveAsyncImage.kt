package org.kimoteru.wallify.core.designsystem.ui.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import kotlin.time.Duration.Companion.milliseconds

/**
 * Image that appears in steps: the dominant [color] of the photo (parsed by the logic of the screen), then the small
 * version [lowUrl], then [highUrl] fades in over it.
 *
 * - The small layer is not requested when it is the same address as [highUrl] (the chosen quality is the lowest) or
 *   when [highUrl] is already in the memory cache of the image loader: the image then shows at once.
 * - The small layer leaves the composition once [highUrl] has faded in, so a card never holds two images.
 * - [placeholderKey] is the memory cache key of an image of the same photo that is already loaded, usually the
 *   address from the list the photo was opened from (the loader keys its memory cache by the address). While
 *   [highUrl] loads, that image is shown in place of the color if it is in memory (the Coil recipe for a shared
 *   element between a list and a detail screen).
 *
 * The images are sized by the view, so a large address never decodes at its full size.
 */
@Composable
fun ProgressiveAsyncImage(
    modifier: Modifier = Modifier,
    color: Color?,
    lowUrl: String?,
    highUrl: String?,
    placeholderKey: String? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val appColor = AppTheme.color
    val appDuration = AppTheme.durationAnim

    val platformContext = LocalPlatformContext.current
    val imageLoader = SingletonImageLoader.get(platformContext)

    val backgroundColor = color ?: appColor.glass.standard
    var isLowShown by remember(lowUrl, highUrl) {
        val isHighInMemory = highUrl != null && imageLoader.memoryCache?.get(MemoryCache.Key(highUrl)) != null
        mutableStateOf(lowUrl != null && lowUrl != highUrl && !isHighInMemory)
    }
    var isHighLoaded by remember(highUrl) { mutableStateOf(false) }
    val highRequest = remember(highUrl, placeholderKey, appDuration.smallExtra) {
        ImageRequest.Builder(platformContext)
            .data(highUrl)
            .placeholderMemoryCacheKey(placeholderKey)
            .crossfade(appDuration.smallExtra)
            .build()
    }

    // The small layer stays under the chosen image until its fade is over, otherwise the color shows through it.
    LaunchedEffect(isHighLoaded) {
        if (!isHighLoaded) return@LaunchedEffect
        delay(appDuration.smallExtra.milliseconds)
        isLowShown = false
    }

    Box(modifier = modifier.background(backgroundColor)) {
        if (isLowShown) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = lowUrl,
                contentDescription = null,
                contentScale = contentScale
            )
        }

        if (highUrl != null) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = highRequest,
                contentDescription = null,
                contentScale = contentScale,
                onSuccess = { isHighLoaded = true }
            )
        }
    }
}