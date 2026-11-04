package org.kimoteru.wallify.feature.random.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.response.handleResponse
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.random.ui.RandomContract.UiEvent
import org.kimoteru.wallify.feature.random.ui.RandomContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.component.get

private const val RANDOM_PHOTOS_COUNT = 30

interface RandomComponent : BaseComponent<UiState, UiEvent>

class RandomDefaultComponent(
    componentContext: ComponentContext,
    private val openPhoto: (photoId: String) -> Unit
) : BaseDefaultComponent<RandomLogic, UiState, UiEvent, RandomNavigationEvent>(
    componentContext = componentContext
),
    RandomComponent {

    override val logic: RandomLogic = instanceKeeper.getOrCreate { get<RandomLogic>() }

    override fun obtainNavigateEvent(event: RandomNavigationEvent) {
        when (event) {
            is RandomNavigationEvent.OpenPhoto -> openPhoto(event.photoId)
        }
    }
}

/** One set of random photos, loaded when the screen is created and replaced by the shuffle button. */
@Factory
class RandomLogic(
    private val photoRepository: PhotoRepository
) : BaseLogic<UiState, UiEvent, RandomNavigationEvent>() {

    private var loadJob: Job? = null

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    override fun onCreate() = load()

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Shuffle -> load()
            is UiEvent.OpenPhoto -> navigate(RandomNavigationEvent.OpenPhoto(event.photoId))
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return

        loadJob = coroutineScope.launch {
            updateIsLoading(true)

            photoRepository.getRandomPhotos(count = RANDOM_PHOTOS_COUNT).handleResponse(
                onSuccess = { photos -> updatePhotos(photos) },
                onError = { updateHasError(true) }
            )

            updateIsLoading(false)
        }
    }

    private fun updateIsLoading(isLoading: Boolean) {
        uiState.update { it.copy(isLoading = isLoading) }
    }

    /** A successful load replaces the photos and clears an earlier error. */
    private fun updatePhotos(photos: List<Photo>) {
        uiState.update { it.copy(photos = photos, hasError = false) }
    }

    private fun updateHasError(hasError: Boolean) {
        uiState.update { it.copy(hasError = hasError) }
    }
}

sealed interface RandomNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : RandomNavigationEvent
}