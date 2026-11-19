package org.kimoteru.wallify.feature.topics.presentation.components

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.domain.photo.forQuality
import org.kimoteru.wallify.core.domain.repository.TopicRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.topic.TopicsOrder
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.photo.PhotoCardUi
import org.kimoteru.wallify.core.presentation.photo.toCardUi
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsContract.UiEvent
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsContract.UiState
import org.koin.core.annotation.Factory
interface TopicsComponent : BaseComponent<UiState, UiEvent> {
    val topics: Flow<PagingData<Topic>>
    val photos: Flow<PagingData<PhotoCardUi>>
}

/** Topics as a row of choices and the photos of the chosen topic below; the screen picks the first topic itself. */
@Factory
class TopicsLogic(
    topicRepository: TopicRepository,
    private val observeUserSettings: ObserveUserSettingsUseCase
) : BaseLogic<UiState, UiEvent, TopicsNavigationEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    val topics: Flow<PagingData<Topic>> = topicRepository
        .getTopicsFlow(order = TopicsOrder.Featured)
        .cachedIn(coroutineScope)

    /** Ready cards for the quality of the lists; joined after `cachedIn`, which may be collected again. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: Flow<PagingData<PhotoCardUi>> = uiState
        .mapDistinctBy { state -> state.selectedSlug }
        .flatMapLatest { slug ->
            if (slug == null) flowOf(PagingData.empty<Photo>()) else topicRepository.getTopicPhotosFlow(slug)
        }
        .cachedIn(coroutineScope)
        .combine(uiState.mapDistinctBy { state -> state.listImageQuality }) { photos, quality ->
            photos.map { photo ->
                photo.toCardUi(
                    imageUrl = photo.urls.forQuality(quality),
                    lowImageUrl = photo.urls.forQuality(ImageQuality.Low)
                )
            }
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
            is UiEvent.SelectTopic -> updateSelectedSlug(event.slug)
            is UiEvent.FirstTopicShown -> selectFirstTopic(event.slug)
            is UiEvent.OpenPhoto -> navigate(TopicsNavigationEvent.OpenPhoto(event.photoId))
            is UiEvent.ListRefreshError -> showListRefreshError(event.list, event.error) { addListRetry() }
        }
    }

    /** The screen opens on the first topic; a topic the user has chosen stays chosen when the list refreshes. */
    private fun selectFirstTopic(slug: String) {
        if (uiState.value.selectedSlug == null) updateSelectedSlug(slug)
    }

    private fun updateSelectedSlug(slug: String?) {
        uiState.update { it.copy(selectedSlug = slug) }
    }

    private fun updateListImageQuality(quality: ImageQuality) {
        uiState.update { it.copy(listImageQuality = quality) }
    }

    /** «Повторить» on a failed refresh of a list: the failed list on screen repeats itself when the count grows. */
    private fun addListRetry() {
        uiState.update { it.copy(listRetries = it.listRetries + 1) }
    }
}

sealed interface TopicsNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : TopicsNavigationEvent
}