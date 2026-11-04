package org.kimoteru.wallify.feature.feed.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.badge.DefaultBadge
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassIconButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSearchField
import org.kimoteru.wallify.core.designsystem.ui.glass.LocalNavigationVisibility
import org.kimoteru.wallify.core.designsystem.ui.glass.ScreenOverlay
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberHideOnScrollConnection
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.photo.PagingPhotoGrid
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.model.photo.ContentFilter
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.SearchOrder
import org.kimoteru.wallify.core.presentation.base.BaseContract
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
        val isFiltersVisible: Boolean = false
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
    val navigationVisibility = LocalNavigationVisibility.current

    // The photos scroll under the top bar, so the list starts below it and the bar floats over the first row.
    val gridPadding = PaddingValues(
        start = screenPadding.start,
        top = screenPadding.top + appSize.control.height + appPadding.medium,
        end = screenPadding.end,
        bottom = screenPadding.bottom
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberHideOnScrollConnection(navigationVisibility))
    ) {
        PagingPhotoGrid(
            modifier = Modifier.fillMaxSize(),
            paging = photos,
            itemKey = { photo -> photo.id },
            emptyText = stringResource(Res.string.common_photos_empty),
            contentPadding = gridPadding
        ) { photo ->
            PhotoCard(
                imageUrl = photo.urls.regular,
                width = photo.width,
                height = photo.height,
                colorHex = photo.color,
                authorName = photo.user?.name,
                authorAvatarUrl = photo.user?.profileImage?.small,
                onClick = { obtainEvent(FeedContract.UiEvent.OpenPhoto(photo.id)) }
            )
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopCenter),
            visible = navigationVisibility.isVisible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
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
                    level = GlassLevel.Floating
                )

                Box {
                    GlassIconButton(
                        icon = AppIcons.Tune,
                        level = GlassLevel.Floating,
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

        if (uiState.isFiltersVisible) {
            ScreenOverlay {
                FeedFiltersOverlay(
                    isSheet = isSheet,
                    filters = uiState.draftFilters,
                    obtainEvent = obtainEvent
                )
            }
        }
    }
}