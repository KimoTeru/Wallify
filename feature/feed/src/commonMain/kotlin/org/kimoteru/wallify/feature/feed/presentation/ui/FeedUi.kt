package org.kimoteru.wallify.feature.feed.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.badge.DefaultBadge
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSearchField
import org.kimoteru.wallify.core.designsystem.ui.glass.ScreenOverlay
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.glass.scrolledPx
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.paging.ListRefreshErrors
import org.kimoteru.wallify.core.designsystem.ui.photo.PagingPhotoGrid
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.designsystem.ui.photo.rememberPhotoGridState
import org.kimoteru.wallify.core.domain.photo.forQuality
import org.kimoteru.wallify.core.model.photo.ContentFilter
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.SearchOrder
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.photo.ContentFilterUi
import org.kimoteru.wallify.core.presentation.photo.PhotoColorUi
import org.kimoteru.wallify.core.presentation.photo.PhotoOrientationUi
import org.kimoteru.wallify.core.presentation.photo.SearchOrderUi
import org.kimoteru.wallify.feature.feed.presentation.components.FeedComponent
import org.kimoteru.wallify.feature.feed.presentation.ui.utils.FeedFiltersOverlay
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_photos_empty
import org.kimoteru.wallify.core.resources.generated.resources.feed_search_hint

object FeedContract {
    @Immutable
    data class UiState(
        val queryInput: String = String.Empty,
        val query: String = String.Empty,
        val filters: SearchFilters = SearchFilters(),
        val draftFilters: SearchFilters = SearchFilters(),
        val isFiltersVisible: Boolean = false,
        val orientations: List<PhotoOrientationUi> = PhotoOrientationUi.entries,
        val colors: List<PhotoColorUi> = PhotoColorUi.entries,
        val orders: List<SearchOrderUi> = SearchOrderUi.entries,
        val contentFilters: List<ContentFilterUi> = ContentFilterUi.entries,
        val listImageQuality: ImageQuality = UserSettings.Default.listImageQuality,
        /** Presses of «Повторить» on a failed refresh of the photos ([UiEvent.ListRefreshError]). */
        val listRetries: Int = 0
    ) : BaseContract.UiState

    /** Search filters; `null` leaves the value to the backend default. */
    @Immutable
    data class SearchFilters(
        val orientation: PhotoOrientation? = null,
        val color: PhotoColor? = null,
        val order: SearchOrder? = null,
        val contentFilter: ContentFilter? = null
    ) {
        val activeCount: Int
            get() = listOfNotNull(orientation, color, order, contentFilter).size
    }

    sealed interface UiEvent : BaseContract.UiEvent {
        data class ChangeQuery(val query: String) : UiEvent
        data object Search : UiEvent
        data object ClearQuery : UiEvent
        data object ShowFilters : UiEvent
        data object HideFilters : UiEvent
        data class SelectOrientation(val orientation: PhotoOrientation?) : UiEvent
        data class SelectColor(val color: PhotoColor?) : UiEvent
        data class SelectOrder(val order: SearchOrder?) : UiEvent
        data class SelectContentFilter(val contentFilter: ContentFilter?) : UiEvent
        data object ResetFilters : UiEvent
        data object ApplyFilters : UiEvent
        data class OpenPhoto(val photoId: String) : UiEvent

        /** The refresh of a list on screen failed over shown photos, or its failure is over (`null`). */
        data class ListRefreshError(val list: Long, val error: ResponseError?) : UiEvent
    }
}

@Preview
@Composable
fun PreviewFeedUi() {
    AppTheme {
        FeedUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : FeedComponent {
                override val photos = flowOf(PagingData.empty<Photo>())

                @Composable
                override fun getUiState(): FeedContract.UiState = FeedContract.UiState()
            }
        )
    }
}

@Composable
fun FeedUi(
    modifier: Modifier = Modifier,
    component: FeedComponent
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val photos = component.photos.collectAsLazyPagingItems()
    val screenPadding = rememberScreenPadding()
    val isSheet = rememberWindowType() == WindowType.Compact
    // Another search is another list: it opens at the top, not at the place where the previous one was left.
    val gridState = rememberPhotoGridState(uiState.query, uiState.filters)

    FloatingBarScreen(
        modifier = modifier,
        scrolledPx = gridState.scrolledPx(),
        bar = {
            Row(
                modifier = Modifier.padding(screenPadding.header),
                horizontalArrangement = Arrangement.spacedBy(appPadding.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSearchField(
                    modifier = Modifier.weight(1f),
                    value = uiState.queryInput,
                    onValueChange = { obtainEvent(FeedContract.UiEvent.ChangeQuery(it)) },
                    hint = stringResource(Res.string.feed_search_hint),
                    onSearch = { obtainEvent(FeedContract.UiEvent.Search) },
                    onClear = { obtainEvent(FeedContract.UiEvent.ClearQuery) },
                    level = GlassLevel.Standard
                )

                Box {
                    GlassIconButton(
                        icon = AppIcons.Tune,
                        level = GlassLevel.Standard,
                        onClick = { obtainEvent(FeedContract.UiEvent.ShowFilters) }
                    )

                    if (uiState.filters.activeCount > 0) {
                        DefaultBadge(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(appSize.icon.small),
                            count = uiState.filters.activeCount
                        )
                    }
                }
            }
        }
    ) { topPadding ->
        ListRefreshErrors(
            retries = uiState.listRetries,
            onChange = { list, error -> obtainEvent(FeedContract.UiEvent.ListRefreshError(list, error)) }
        ) {
            PagingPhotoGrid(
                modifier = Modifier.fillMaxSize(),
                paging = photos,
                itemKey = { photo -> photo.id },
                emptyText = stringResource(Res.string.common_photos_empty),
                state = gridState,
                contentPadding = PaddingValues(
                    start = screenPadding.start,
                    top = topPadding,
                    end = screenPadding.end,
                    bottom = screenPadding.bottom
                )
            ) { photo ->
                PhotoCard(
                    imageUrl = photo.urls.forQuality(uiState.listImageQuality),
                    lowImageUrl = photo.urls.forQuality(ImageQuality.Low),
                    width = photo.width,
                    height = photo.height,
                    colorHex = photo.color,
                    authorName = photo.user?.name,
                    authorAvatarUrl = photo.user?.profileImage?.small,
                    onClick = { obtainEvent(FeedContract.UiEvent.OpenPhoto(photo.id)) }
                )
            }
        }
    }

    if (uiState.isFiltersVisible) {
        ScreenOverlay {
            FeedFiltersOverlay(
                isSheet = isSheet,
                uiState = uiState,
                obtainEvent = obtainEvent
            )
        }
    }
}