package org.kimoteru.wallify.feature.random.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberIsShortWindow
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.scrolledPx
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.layout.ErrorContentLayout
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridLayout
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridShimmer
import org.kimoteru.wallify.core.designsystem.ui.photo.rememberPhotoGridState
import org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh.DefaultPullToRefreshBox
import org.kimoteru.wallify.core.domain.photo.forQuality
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.feature.random.presentation.components.RandomComponent
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.nav_random
import org.kimoteru.wallify.core.resources.generated.resources.random_shuffle

object RandomContract {
    @Immutable
    data class UiState(
        val photos: List<Photo> = emptyList(),
        val isLoading: Boolean = false,
        /** Failure of the last load; the screen picks its text by the type and shows the code. */
        val error: ResponseError? = null,
        val listImageQuality: ImageQuality = UserSettings.Default.listImageQuality
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
    // A shuffle brings a new set: it opens at the top, not where the previous one was left.
    val gridState = rememberPhotoGridState(uiState.photos.firstOrNull()?.id)

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
            isFailed = uiState.error != null && uiState.photos.isEmpty(),
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

                uiState.error != null && uiState.photos.isEmpty() -> ErrorContentLayout(
                    error = uiState.error,
                    onRetry = { obtainEvent(RandomContract.UiEvent.Shuffle) }
                )

                // Pulling the set down is the same as the shuffle button: a new set.
                else -> DefaultPullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = uiState.isLoading,
                    onRefresh = { obtainEvent(RandomContract.UiEvent.Shuffle) },
                    indicatorPadding = PaddingValues(top = topPadding)
                ) {
                    PhotoGridLayout(
                        modifier = Modifier.fillMaxSize(),
                        state = gridState,
                        contentPadding = listPadding
                    ) {
                        items(
                            items = uiState.photos,
                            key = { photo -> photo.id }
                        ) { photo ->
                            PhotoCard(
                                imageUrl = photo.urls.forQuality(uiState.listImageQuality),
                                lowImageUrl = photo.urls.forQuality(ImageQuality.Low),
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
}