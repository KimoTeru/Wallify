package org.kimoteru.wallify.feature.topics.presentation.components

import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.domain.repository.TopicRepository
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.topic.TopicsOrder
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsContract.UiEvent
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsContract.UiState
import org.koin.core.annotation.Factory
interface TopicsComponent : BaseComponent<UiState, UiEvent> {
    val topics: Flow<PagingData<Topic>>
    val photos: Flow<PagingData<Photo>>
}

/** Topics as a row of choices and the photos of the chosen topic below; the screen picks the first topic itself. */
@Factory
class TopicsLogic(
    topicRepository: TopicRepository
) : BaseLogic<UiState, UiEvent, TopicsNavigationEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    val topics: Flow<PagingData<Topic>> = topicRepository
        .getTopicsFlow(order = TopicsOrder.Featured)
        .cachedIn(coroutineScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: Flow<PagingData<Photo>> = uiState
        .mapDistinctBy { state -> state.selectedSlug }
        .flatMapLatest { slug ->
            if (slug == null) flowOf(PagingData.empty<Photo>()) else topicRepository.getTopicPhotosFlow(slug)
        }
        .cachedIn(coroutineScope)

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SelectTopic -> updateSelectedSlug(event.slug)
            is UiEvent.OpenPhoto -> navigate(TopicsNavigationEvent.OpenPhoto(event.photoId))
        }
    }

    private fun updateSelectedSlug(slug: String?) {
        uiState.update { it.copy(selectedSlug = slug) }
    }
}

sealed interface TopicsNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : TopicsNavigationEvent
}