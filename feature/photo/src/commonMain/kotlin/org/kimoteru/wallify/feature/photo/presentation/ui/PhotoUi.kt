package org.kimoteru.wallify.feature.photo.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.full_screen_loader.DefaultFullScreenLoader
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.layout.ErrorContentLayout
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.feature.photo.presentation.components.PhotoComponent
import org.kimoteru.wallify.feature.photo.presentation.ui.utils.PhotoCompactContent
import org.kimoteru.wallify.feature.photo.presentation.ui.utils.PhotoWideContent

object PhotoContract {
    @Immutable
    data class UiState(
        val photo: Photo? = null,
        val statistics: Statistics? = null,
        val collections: List<PhotoCollection> = emptyList(),
        val memberCollectionIds: Set<Int> = emptySet(),
        val pendingCollectionIds: Set<Int> = emptySet(),
        val isLoading: Boolean = true,
        /** Failure of the photo load; the screen picks its text by the type and shows the code. */
        val error: ResponseError? = null,
        val isDownloading: Boolean = false,
        val detailImageQuality: ImageQuality = UserSettings.Default.detailImageQuality,

        /** Quality of the lists: the image already loaded there stands in while the chosen one loads. */
        val listImageQuality: ImageQuality = UserSettings.Default.listImageQuality
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
    val screenPadding = rememberScreenPadding()

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val uriHandler = LocalUriHandler.current
    val windowType = rememberWindowType()
    val compactScroll = rememberScrollState()
    val wideScroll = rememberScrollState()
    val scroll = if (windowType == WindowType.Compact) compactScroll else wideScroll

    component.collectComponentEvents { event ->
        when (event) {
            is PhotoContract.ComponentEvent.OpenUrl -> uriHandler.openUri(event.url)
        }
    }

    // A photo that failed to load offline loads again by itself when the connection returns.
    RetryOnReconnect(
        isFailed = uiState.photo == null && uiState.error != null,
        onRetry = { obtainEvent(PhotoContract.UiEvent.Retry) }
    )

    FloatingBarScreen(
        modifier = modifier.background(appColor.basic.background),
        // On a wide window the inspector scroll must not frost the bar: that blur spreads over the photo.
        scrolledPx = if (windowType == WindowType.Compact) scroll.value else 0,
        bar = {
            DefaultTopBar(
                paddingValues = screenPadding.header,
                onBack = { component.obtainBaseEvent(BaseContract.UiEvent.NavigateBack) }
            )
        }
    ) { _ ->
        val photo = uiState.photo

        when {
            photo != null && windowType == WindowType.Compact -> PhotoCompactContent(
                photo = photo,
                uiState = uiState,
                scroll = compactScroll,
                obtainEvent = obtainEvent
            )

            photo != null -> PhotoWideContent(
                photo = photo,
                uiState = uiState,
                scroll = wideScroll,
                obtainEvent = obtainEvent
            )

            uiState.isLoading -> DefaultFullScreenLoader(modifier = Modifier.fillMaxSize())

            else -> ErrorContentLayout(
                error = uiState.error,
                onRetry = { obtainEvent(PhotoContract.UiEvent.Retry) }
            )
        }
    }
}