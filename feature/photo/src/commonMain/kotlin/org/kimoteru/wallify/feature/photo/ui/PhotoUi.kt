package org.kimoteru.wallify.feature.photo.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.full_screen_loader.DefaultFullScreenLoader
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurSourceZIndex
import org.kimoteru.wallify.core.designsystem.ui.blur.blurSource
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.image.CachedAsyncImage
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorDescription
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorTitle
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMessage
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_retry
private const val COMPACT_MIN_IMAGE_RATIO = 0.6f
private const val COMPACT_MAX_IMAGE_RATIO = 1.5f

object PhotoContract {
    @Immutable
    data class UiState(
        val photo: Photo? = null,
        val statistics: Statistics? = null,
        val collections: List<PhotoCollection> = emptyList(),
        val memberCollectionIds: Set<Int> = emptySet(),
        val pendingCollectionIds: Set<Int> = emptySet(),
        val isLoading: Boolean = true,
        val hasError: Boolean = false,
        val isDownloading: Boolean = false
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object Retry : UiEvent
        data object Download : UiEvent
        data object OpenSource : UiEvent
        data object OpenAuthor : UiEvent
        data class ToggleCollection(val collectionId: Int) : UiEvent
    }

    sealed interface ComponentEvent : BaseContract.ComponentEvent {
        data class OpenUrl(val url: String) : ComponentEvent
    }
}

@Preview
@Composable
fun PreviewPhotoUi() {
    AppTheme {
        PhotoUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : PhotoComponent {
                @Composable
                override fun getUiState(): PhotoContract.UiState = PhotoContract.UiState()
            }
        )
    }
}

@Composable
fun PhotoUi(
    modifier: Modifier = Modifier,
    component: PhotoComponent
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val uriHandler = LocalUriHandler.current
    val windowType = rememberWindowType()

    component.collectComponentEvents { event ->
        when (event) {
            is PhotoContract.ComponentEvent.OpenUrl -> uriHandler.openUri(event.url)
        }
    }

    // A photo that failed to load offline loads again by itself when the connection returns.
    RetryOnReconnect(
        isFailed = uiState.photo == null && uiState.hasError,
        onRetry = { obtainEvent(PhotoContract.UiEvent.Retry) }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appColor.basic.background)
    ) {
        val photo = uiState.photo

        when {
            photo != null && windowType == WindowType.Compact -> PhotoCompactContent(
                photo = photo,
                uiState = uiState,
                obtainEvent = obtainEvent
            )

            photo != null -> PhotoWideContent(
                photo = photo,
                uiState = uiState,
                obtainEvent = obtainEvent
            )

            uiState.isLoading -> DefaultFullScreenLoader(modifier = Modifier.fillMaxSize())

            else -> PhotoGridMessage(
                title = networkErrorTitle(),
                description = networkErrorDescription(),
                actionText = stringResource(Res.string.common_retry),
                onAction = { obtainEvent(PhotoContract.UiEvent.Retry) }
            )
        }

        GlassIconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(appPadding.medium),
            icon = AppIcons.Back,
            level = GlassLevel.Floating,
            onClick = { component.obtainBaseEvent(BaseContract.UiEvent.NavigateBack) }
        )
    }
}

/** Phone: the photo at the top, everything else under it in one scroll. */
@Composable
private fun PhotoCompactContent(
    photo: Photo,
    uiState: PhotoContract.UiState,
    obtainEvent: (PhotoContract.UiEvent) -> Unit
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding

    val imageRatio = if (photo.width > 0 && photo.height > 0) photo.width / photo.height.toFloat() else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CachedAsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageRatio.coerceIn(COMPACT_MIN_IMAGE_RATIO, COMPACT_MAX_IMAGE_RATIO))
                .clip(
                    RoundedCornerShape(
                        bottomStart = appCorner.large + appCorner.small,
                        bottomEnd = appCorner.large + appCorner.small
                    )
                )
                .background(appColor.glass.sub)
                .blurSource(BlurSourceZIndex.Screen),
            model = photo.urls.regular
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
private fun PhotoWideContent(
    photo: Photo,
    uiState: PhotoContract.UiState,
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
                .background(appColor.glass.sub)
                .blurSource(BlurSourceZIndex.Screen)
        ) {
            CachedAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = photo.urls.regular,
                contentScale = ContentScale.Fit
            )
        }

        PhotoDetails(
            modifier = Modifier
                .width(appSize.panel.inspectorWidth)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = appPadding.large, end = appPadding.medium, bottom = appPadding.medium),
            photo = photo,
            uiState = uiState,
            obtainEvent = obtainEvent
        )
    }
}