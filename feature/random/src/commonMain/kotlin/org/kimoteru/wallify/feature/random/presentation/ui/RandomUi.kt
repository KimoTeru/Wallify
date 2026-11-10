package org.kimoteru.wallify.feature.random.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberIsShortWindow
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.scrolledPx
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorDescription
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorTitle
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridLayout
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMessage
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridShimmer
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.feature.random.presentation.components.RandomComponent
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_retry
import org.kimoteru.wallify.core.resources.generated.resources.nav_random
import org.kimoteru.wallify.core.resources.generated.resources.random_shuffle

object RandomContract {
    @Immutable
    data class UiState(
        val photos: List<Photo> = emptyList(),
        val isLoading: Boolean = false,
        val hasError: Boolean = false
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object Shuffle : UiEvent
        data class OpenPhoto(val photoId: String) : UiEvent
    }
}

@Preview
@Composable
fun PreviewRandomUi() {
    AppTheme {
        RandomUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : RandomComponent {
                @Composable
                override fun getUiState(): RandomContract.UiState = RandomContract.UiState()
            }
        )
    }
}

@Composable
fun RandomUi(
    modifier: Modifier = Modifier,
    component: RandomComponent
) {
    val appTypography = AppTheme.typography
    val isShortWindow = rememberIsShortWindow()

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val screenPadding = rememberScreenPadding()
    val gridState = rememberLazyStaggeredGridState()

    FloatingBarScreen(
        modifier = modifier,
        scrolledPx = gridState.scrolledPx(),
        bar = {
            DefaultTopBar(
                title = stringResource(Res.string.nav_random),
                titleStyle = if (isShortWindow) appTypography.h5.semiBold else appTypography.h2.bold,
                paddingValues = screenPadding.header,
                actions = {
                    GlassButton(
                        text = stringResource(Res.string.random_shuffle),
                        icon = AppIcons.Refresh,
                        enabled = !uiState.isLoading,
                        onClick = { obtainEvent(RandomContract.UiEvent.Shuffle) }
                    )
                }
            )
        }
    ) { topPadding ->
        // Goes back to the photos when the connection returns after a failed load.
        RetryOnReconnect(
            isFailed = uiState.hasError && uiState.photos.isEmpty(),
            onRetry = { obtainEvent(RandomContract.UiEvent.Shuffle) }
        )

        val listPadding = PaddingValues(
            start = screenPadding.start,
            top = topPadding,
            end = screenPadding.end,
            bottom = screenPadding.bottom
        )

        // A set of random photos is shown as a grid of equal cards whatever layout the feed uses.
        CompositionLocalProvider(LocalPhotoGridMode provides PhotoGridMode.Grid) {
            when {
                uiState.isLoading -> PhotoGridShimmer(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding
                )

                uiState.hasError && uiState.photos.isEmpty() -> PhotoGridMessage(
                    title = networkErrorTitle(),
                    description = networkErrorDescription(),
                    actionText = stringResource(Res.string.common_retry),
                    onAction = { obtainEvent(RandomContract.UiEvent.Shuffle) }
                )

                else -> PhotoGridLayout(
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    contentPadding = listPadding
                ) {
                    items(
                        items = uiState.photos,
                        key = { photo -> photo.id }
                    ) { photo ->
                        PhotoCard(
                            imageUrl = photo.urls.regular,
                            width = photo.width,
                            height = photo.height,
                            colorHex = photo.color,
                            authorName = photo.user?.name,
                            authorAvatarUrl = photo.user?.profileImage?.small,
                            onClick = { obtainEvent(RandomContract.UiEvent.OpenPhoto(photo.id)) }
                        )
                    }
                }
            }
        }
    }
}