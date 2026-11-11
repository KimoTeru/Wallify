package org.kimoteru.wallify.feature.random.presentation.components

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.handleResponse
import org.kimoteru.wallify.core.domain.photo.forQuality
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.photo.PhotoCardUi
import org.kimoteru.wallify.core.presentation.photo.toCardUi
import org.kimoteru.wallify.feature.random.presentation.ui.RandomContract.UiEvent
import org.kimoteru.wallify.feature.random.presentation.ui.RandomContract.UiState
import org.koin.core.annotation.Factory
private const val RANDOM_PHOTOS_COUNT = 30

interface RandomComponent : BaseComponent<UiState, UiEvent>

/**
 * One set of random photos. Opening the screen shows the set saved a few minutes ago; the shuffle button (and the
 * retry after a failure) always asks for a new one.
 */
@Factory
class RandomLogic(
    private val photoRepository: PhotoRepository,
    private val observeUserSettings: ObserveUserSettingsUseCase
) : BaseLogic<UiState, UiEvent, RandomNavigationEvent>() {

    private var loadJob: Job? = null

    /** The last loaded set; the cards on screen are built from it for the quality of the lists. */
    private val loadedPhotos = MutableStateFlow<List<Photo>>(emptyList())

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                combine(
                    loadedPhotos,
                    observeUserSettings().mapDistinctBy { settings -> settings.listImageQuality }
                ) { photos, quality ->
                    photos.map { photo ->
                        photo.toCardUi(
                            imageUrl = photo.urls.forQuality(quality),
                            lowImageUrl = photo.urls.forQuality(ImageQuality.Low)
                        )
                    }
                }.collect { cards -> updatePhotos(cards) }
            }
        }

        load(forceRefresh = false)
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Shuffle -> load(forceRefresh = true)
            is UiEvent.OpenPhoto -> navigate(RandomNavigationEvent.OpenPhoto(event.photoId))
        }
    }

    private fun load(forceRefresh: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = coroutineScope.launch {
            updateIsLoading(true)

            photoRepository.getRandomPhotos(count = RANDOM_PHOTOS_COUNT, forceRefresh = forceRefresh).handleResponse(
                onSuccess = { photos -> showLoadedPhotos(photos) },
                onError = { error ->
                    updateError(error)
                    // The photos on screen stay: a failed shuffle goes to the snackbar, not in place of them.
                    if (uiState.value.photos.isNotEmpty()) showErrorWithRetry(error) { load(forceRefresh = true) }
                }
            )

            updateIsLoading(false)
        }
    }

    private fun updateIsLoading(isLoading: Boolean) {
        uiState.update { it.copy(isLoading = isLoading) }
    }

    /** A successful load replaces the set (its cards follow) and clears an earlier error. */
    private fun showLoadedPhotos(photos: List<Photo>) {
        loadedPhotos.value = photos
        updateError(null)
    }

    private fun updatePhotos(photos: List<PhotoCardUi>) {
        uiState.update { it.copy(photos = photos) }
    }

    private fun updateError(error: ResponseError?) {
        uiState.update { it.copy(error = error) }
    }
}

sealed interface RandomNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : RandomNavigationEvent
}