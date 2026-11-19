package org.kimoteru.wallify.feature.photo.presentation.ui.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.image.ProgressiveAsyncImage
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract

private const val COMPACT_MIN_IMAGE_RATIO = 0.6f
private const val COMPACT_MAX_IMAGE_RATIO = 1.5f

/** Phone: the photo at the top, everything else under it in one scroll. */
@Composable
internal fun PhotoCompactContent(
    photo: Photo,
    image: PhotoContract.PhotoImageUi,
    uiState: PhotoContract.UiState,
    scroll: ScrollState,
    obtainEvent: (PhotoContract.UiEvent) -> Unit
) {
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        PhotoImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(image.aspectRatio.coerceIn(COMPACT_MIN_IMAGE_RATIO, COMPACT_MAX_IMAGE_RATIO))
                .clip(
                    RoundedCornerShape(
                        bottomStart = appCorner.large + appCorner.small,
                        bottomEnd = appCorner.large + appCorner.small
                    )
                ),
            image = image,
            contentScale = ContentScale.Crop
        )

        PhotoDetails(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(appPadding.medium),
            photo = photo,
            uiState = uiState,
            obtainEvent = obtainEvent
        )
    }
}

/** Tablet and desktop: the whole photo on the left, the inspector with its own scroll on the right. */
@Composable
internal fun PhotoWideContent(
    photo: Photo,
    image: PhotoContract.PhotoImageUi,
    uiState: PhotoContract.UiState,
    scroll: ScrollState,
    obtainEvent: (PhotoContract.UiEvent) -> Unit
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(appPadding.medium)
                .clip(RoundedCornerShape(appCorner.large + appCorner.smallSpecial))
                .background(appColor.basic.background)
        ) {
            // The color of the photo fills the shape of the photo, not the whole panel around it.
            PhotoImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .aspectRatio(image.aspectRatio),
                image = image,
                contentScale = ContentScale.Fit
            )
        }

        PhotoDetails(
            modifier = Modifier
                .width(appSize.panel.inspectorWidth)
                .fillMaxHeight()
                .verticalScroll(scroll)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    top = appPadding.large,
                    start = appPadding.medium,
                    end = appPadding.medium,
                    bottom = appPadding.medium
                ),
            photo = photo,
            uiState = uiState,
            obtainEvent = obtainEvent
        )
    }
}

/**
 * The photo in the quality chosen for this screen: its color, then the small version, then the chosen one. While it
 * loads, the image from the list the photo was opened from (already in memory) stands in. The addresses come ready
 * from the logic ([PhotoContract.PhotoImageUi]).
 */
@Composable
private fun PhotoImage(
    modifier: Modifier = Modifier,
    image: PhotoContract.PhotoImageUi,
    contentScale: ContentScale
) {
    ProgressiveAsyncImage(
        modifier = modifier,
        color = image.colorArgb?.let(::Color),
        lowUrl = image.lowUrl,
        highUrl = image.highUrl,
        placeholderKey = image.placeholderKey,
        contentScale = contentScale
    )
}