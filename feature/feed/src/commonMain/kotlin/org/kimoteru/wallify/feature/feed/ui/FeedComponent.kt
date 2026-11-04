package org.kimoteru.wallify.feature.feed.ui

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.domain.repository.SearchRepository
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.feed.ui.FeedContract.SearchFilters
import org.kimoteru.wallify.feature.feed.ui.FeedContract.UiEvent
import org.kimoteru.wallify.feature.feed.ui.FeedContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.component.get

interface FeedComponent : BaseComponent<UiState, UiEvent> {
    val photos: Flow<PagingData<Photo>>
}

class FeedDefaultComponent(
    componentContext: ComponentContext,
    private val openPhoto: (photoId: String) -> Unit
) : BaseDefaultComponent<FeedLogic, UiState, UiEvent, FeedNavigationEvent>(
    componentContext = componentContext
),
    FeedComponent {

    override val logic: FeedLogic = instanceKeeper.getOrCreate { get<FeedLogic>() }

    override val photos: Flow<PagingData<Photo>> = logic.photos

    override fun obtainNavigateEvent(event: FeedNavigationEvent) {
        when (event) {
            is FeedNavigationEvent.OpenPhoto -> openPhoto(event.photoId)
        }
    }
}

/**
 * The editorial feed, or the search results once a search text is submitted. The filters are chosen in a draft and
 * reach the search only when applied; without a search text the backend has nothing to filter, so the feed stays
 * the same.
 */
@Factory
class FeedLogic(
    private val photoRepository: PhotoRepository,
    private val searchRepository: SearchRepository
) : BaseLogic<UiState, UiEvent, FeedNavigationEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: Flow<PagingData<Photo>> = uiState
        .mapDistinctBy { state -> SearchRequest(state.query, state.filters) }
        .flatMapLatest { request ->
            if (request.query.isBlank()) {
                photoRepository.getPhotosFlow()
            } else {
                searchRepository.searchPhotosFlow(request.toQuery())
            }
        }
        .cachedIn(coroutineScope)

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ChangeQuery -> updateQueryInput(event.query)
            is UiEvent.Search -> updateQuery(uiState.value.queryInput.trim())
            is UiEvent.ClearQuery -> clearQuery()
            is UiEvent.ShowFilters -> showFilters()
            is UiEvent.HideFilters -> updateIsFiltersVisible(false)
            is UiEvent.SelectOrientation -> updateDraftFilters(
                uiState.value.draftFilters.copy(orientation = event.orientation)
            )
            is UiEvent.SelectColor -> updateDraftFilters(uiState.value.draftFilters.copy(color = event.color))
            is UiEvent.SelectOrder -> updateDraftFilters(uiState.value.draftFilters.copy(order = event.order))
            is UiEvent.SelectContentFilter -> updateDraftFilters(
                uiState.value.draftFilters.copy(contentFilter = event.contentFilter)
            )
            is UiEvent.ResetFilters -> updateDraftFilters(SearchFilters())
            is UiEvent.ApplyFilters -> updateFilters(uiState.value.draftFilters)
            is UiEvent.OpenPhoto -> navigate(FeedNavigationEvent.OpenPhoto(event.photoId))
        }
    }

    private fun clearQuery() {
        uiState.update { it.copy(queryInput = String.Empty, query = String.Empty) }
    }

    /** The draft starts from the applied filters every time the sheet opens. */
    private fun showFilters() {
        uiState.update { it.copy(isFiltersVisible = true, draftFilters = it.filters) }
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