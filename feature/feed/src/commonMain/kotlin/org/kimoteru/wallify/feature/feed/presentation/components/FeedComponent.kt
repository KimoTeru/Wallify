package org.kimoteru.wallify.feature.feed.presentation.components

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.domain.photo.forQuality
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.domain.repository.SearchRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.photo.PhotoCardUi
import org.kimoteru.wallify.core.presentation.photo.toCardUi
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedContract.SearchFilters
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedContract.UiEvent
import org.kimoteru.wallify.feature.feed.presentation.ui.FeedContract.UiState
import org.koin.core.annotation.Factory
interface FeedComponent : BaseComponent<UiState, UiEvent> {
    val photos: Flow<PagingData<PhotoCardUi>>
}

/**
 * The editorial feed, or the search results once a search text is submitted. The filters are chosen in a draft and
 * reach the search only when applied; without a search text the backend has nothing to filter, so the feed stays
 * the same.
 */
@Factory
class FeedLogic(
    private val photoRepository: PhotoRepository,
    private val searchRepository: SearchRepository,
    private val observeUserSettings: ObserveUserSettingsUseCase
) : BaseLogic<UiState, UiEvent, FeedNavigationEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    /**
     * The cards are built here, not in the Ui: the addresses for the quality, the color, the proportions and the
     * initials of the author. The quality is joined after `cachedIn`: a cached `PagingData` may be collected again,
     * one straight from the pager may not.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: Flow<PagingData<PhotoCardUi>> = uiState
        .mapDistinctBy { state -> SearchRequest(state.query, state.filters) }
        .flatMapLatest { request ->
            if (request.query.isBlank()) {
                photoRepository.getPhotosFlow()
            } else {
                searchRepository.searchPhotosFlow(request.toQuery())
            }
        }
        .cachedIn(coroutineScope)
        .combine(uiState.mapDistinctBy { state -> state.listImageQuality }) { photos, quality ->
            photos.map { photo -> photo.toCard(quality) }
        }

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                observeUserSettings()
                    .mapDistinctBy { settings -> settings.listImageQuality }
                    .collect { quality -> updateListImageQuality(quality) }
            }
        }
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ChangeQuery -> updateQueryInput(event.query)
            is UiEvent.Search -> updateQuery(uiState.value.queryInput.trim())
            is UiEvent.ClearQuery -> clearQuery()
            is UiEvent.ShowFilters -> showFilters()
            is UiEvent.HideFilters -> updateIsFiltersVisible(false)
            is UiEvent.ToggleOrientation -> toggleOrientation(event.orientation)
            is UiEvent.ToggleColor -> toggleColor(event.color)
            is UiEvent.SelectOrder -> updateDraftFilters(uiState.value.draftFilters.copy(order = event.order))
            is UiEvent.SelectContentFilter -> updateDraftFilters(
                uiState.value.draftFilters.copy(contentFilter = event.contentFilter)
            )
            is UiEvent.ResetFilters -> updateDraftFilters(SearchFilters())
            is UiEvent.ApplyFilters -> updateFilters(uiState.value.draftFilters)
            is UiEvent.OpenPhoto -> navigate(FeedNavigationEvent.OpenPhoto(event.photoId))
            is UiEvent.ListRefreshError -> showListRefreshError(event.list, event.error) { addListRetry() }
        }
    }

    private fun clearQuery() {
        uiState.update { it.copy(queryInput = String.Empty, query = String.Empty) }
    }

    /** The draft starts from the applied filters every time the sheet opens. */
    private fun showFilters() {
        uiState.update { it.copy(isFiltersVisible = true, draftFilters = it.filters) }
    }

    /** The chosen orientation chip, tapped again, leaves the orientation to the backend. */
    private fun toggleOrientation(orientation: PhotoOrientation) {
        val draftFilters = uiState.value.draftFilters
        val chosen = orientation.takeIf { it != draftFilters.orientation }
        updateDraftFilters(draftFilters.copy(orientation = chosen))
    }

    /** The chosen color chip, tapped again, leaves the color to the backend. */
    private fun toggleColor(color: PhotoColor) {
        val draftFilters = uiState.value.draftFilters
        val chosen = color.takeIf { it != draftFilters.color }
        updateDraftFilters(draftFilters.copy(color = chosen))
    }

    private fun updateQueryInput(queryInput: String) {
        uiState.update { it.copy(queryInput = queryInput) }
    }

    private fun updateQuery(query: String) {
        uiState.update { it.copy(query = query) }
    }

    private fun updateIsFiltersVisible(isVisible: Boolean) {
        uiState.update { it.copy(isFiltersVisible = isVisible) }
    }

    private fun updateDraftFilters(draftFilters: SearchFilters) {
        uiState.update { it.copy(draftFilters = draftFilters) }
    }

    private fun updateFilters(filters: SearchFilters) {
        uiState.update { it.copy(filters = filters) }
    }

    private fun updateListImageQuality(quality: ImageQuality) {
        uiState.update { it.copy(listImageQuality = quality) }
    }

    /** «Повторить» on a failed refresh of the photos: the list on screen repeats itself when the count grows. */
    private fun addListRetry() {
        uiState.update { it.copy(listRetries = it.listRetries + 1) }
    }

    private fun Photo.toCard(quality: ImageQuality): PhotoCardUi = toCardUi(
        imageUrl = urls.forQuality(quality),
        lowImageUrl = urls.forQuality(ImageQuality.Low)
    )

    private data class SearchRequest(
        val query: String,
        val filters: SearchFilters
    ) {
        fun toQuery() = SearchPhotosQuery(
            query = query,
            order = filters.order,
            contentFilter = filters.contentFilter,
            color = filters.color,
            orientation = filters.orientation
        )
    }
}

sealed interface FeedNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : FeedNavigationEvent
}