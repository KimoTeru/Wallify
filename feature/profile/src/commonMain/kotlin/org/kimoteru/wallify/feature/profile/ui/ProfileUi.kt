package org.kimoteru.wallify.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import kotlinx.coroutines.flow.flowOf
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalNavigationVisibility
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberHideOnScrollConnection
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseContract

object ProfileContract {
    enum class Tab { Photos, Collections }

    @Immutable
    data class UiState(
        val user: User? = null,
        val statistics: Statistics? = null,
        val selectedTab: Tab = Tab.Photos,
        val openedCollection: PhotoCollection? = null,
        val isLoading: Boolean = true,
        val hasError: Boolean = false
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object Retry : UiEvent
        data class SelectTab(val tab: Tab) : UiEvent
        data class OpenCollection(val collection: PhotoCollection) : UiEvent
        data object CloseCollection : UiEvent
        data class OpenPhoto(val photoId: String) : UiEvent
    }
}

@Preview
@Composable
fun PreviewProfileUi() {
    AppTheme {
        ProfileUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : ProfileComponent {
                override val canNavigateBack = false
                override val photos = flowOf(PagingData.empty<Photo>())
                override val collections = flowOf(PagingData.empty<PhotoCollection>())
                override val collectionPhotos = flowOf(PagingData.empty<Photo>())

                @Composable
                override fun getUiState(): ProfileContract.UiState = ProfileContract.UiState()
            }
        )
    }
}

@Composable
fun ProfileUi(
    modifier: Modifier = Modifier,
    component: ProfileComponent
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val screenPadding = rememberScreenPadding()

    // A back button floats over the top left corner, so the content starts below it.
    val topPadding = if (component.canNavigateBack) {
        screenPadding.top + appSize.control.iconButton + appPadding.small
    } else {
        screenPadding.top
    }
    val contentPadding = PaddingValues(
        start = screenPadding.start,
        top = topPadding,
        end = screenPadding.end,
        bottom = screenPadding.bottom
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberHideOnScrollConnection(LocalNavigationVisibility.current))
    ) {
        val header: @Composable () -> Unit = {
            ProfileHeader(
                uiState = uiState,
                obtainEvent = obtainEvent
            )
        }

        RetryOnReconnect(
            isFailed = uiState.hasError && uiState.user == null,
            onRetry = { obtainEvent(ProfileContract.UiEvent.Retry) }
        )

        // Photos and collections of a profile are grids of equal cards whatever layout the feed uses.
        CompositionLocalProvider(LocalPhotoGridMode provides PhotoGridMode.Grid) {
            when {
                uiState.selectedTab == ProfileContract.Tab.Photos -> ProfilePhotosGrid(
                    photos = component.photos,
                    contentPadding = contentPadding,
                    header = header,
                    obtainEvent = obtainEvent
                )

                uiState.openedCollection != null -> ProfilePhotosGrid(
                    photos = component.collectionPhotos,
                    contentPadding = contentPadding,
                    header = header,
                    collection = uiState.openedCollection,
                    obtainEvent = obtainEvent
                )

                else -> ProfileCollectionsGrid(
                    collections = component.collections,
                    contentPadding = contentPadding,
                    header = header,
                    obtainEvent = obtainEvent
                )
            }
        }

        if (component.canNavigateBack) {
            GlassIconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(appPadding.medium),
                icon = AppIcons.Back,
                onClick = { component.obtainBaseEvent(BaseContract.UiEvent.NavigateBack) }
            )
        }
    }
}