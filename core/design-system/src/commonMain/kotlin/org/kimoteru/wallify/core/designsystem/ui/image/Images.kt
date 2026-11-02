package org.kimoteru.wallify.core.designsystem.ui.image

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun CachedAsyncImage(
    modifier: Modifier = Modifier,
    model: Any?,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model == null) {
        Box(modifier = modifier)
    } else {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}