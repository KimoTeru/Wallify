package org.kimoteru.wallify.feature.photo.presentation.components

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.handleResponse
import org.kimoteru.wallify.core.common.response.onError
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.presentation.base.BaseComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseLogicCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_dialog_description
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.ComponentEvent
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiEvent
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

private const val STATISTICS_DAYS = 30

interface PhotoComponent : BaseComponentCore<UiState, UiEvent, ComponentEvent>

/**
 * Loads the whole photo, its 30-day statistics and the signed-in user's collections independently, so a failing
 * extra never hides the photo. The download goes through the browser: the logic only reports the download to the
 * backend and hands the address of the file to the screen.
 */
@Factory
class PhotoLogic(
    @InjectedParam private val photoId: String,
    private val photoRepository: PhotoRepository,
    private val userRepository: UserRepository,
    private val collectionRepository: CollectionRepository,
    private val notifierManager: NotifierManager,
    private val observeUserSettings: ObserveUserSettingsUseCase
) : BaseLogicCore<UiState, UiEvent, PhotoNavigationEvent, ComponentEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    private var photoJob: Job? = null

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                observeUserSettings()
                    .mapDistinctBy { settings -> settings.detailImageQuality to settings.listImageQuality }
                    .collect { (detailQuality, listQuality) ->
                        uiState.update { it.copy(detailImageQuality = detailQuality, listImageQuality = listQuality) }
                    }
            }
        }

        loadPhoto()
        loadStatistics()
        loadCollections()
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Retry -> loadPhoto()
            is UiEvent.Download -> download()
            is UiEvent.OpenSource -> uiState.value.photo?.htmlUrl?.let { url ->
                sendComponentEvent(ComponentEvent.OpenUrl(url))
            }
            is UiEvent.OpenAuthor -> uiState.value.photo?.user?.username?.let { username ->
                navigate(PhotoNavigationEvent.OpenProfile(username))
            }
            is UiEvent.ToggleCollection -> toggleCollection(event.collectionId)
        }
    }

    /** Follows the photo in the database: the saved one first, then the one the backend answered with. */
    private fun loadPhoto() {
        photoJob?.cancel()
        photoJob = coroutineScope.launch {
            photoRepository.getPhotoFlow(photoId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        updateHasError(false)
                        updateIsLoading(true)
                    }
                    is Resource.Success -> {
                        showPhoto(resource.data)
                        updateIsLoading(false)
                    }
                    is Resource.Error -> {
                        updateHasError(true)
                        updateIsLoading(false)
                    }
                }
            }
        }
    }

    private fun loadStatistics() {
        coroutineScope.launch {
            photoRepository.getPhotoStatisticsFlow(photoId, STATISTICS_DAYS).collect { resource ->
                if (resource is Resource.Success) updateStatistics(resource.data)
            }
        }
    }

    private fun loadCollections() {
        val username = userRepository.currentUser.value?.username ?: return

        coroutineScope.launch {
            userRepository.getUserCollectionListFlow(username).collect { resource ->
                if (resource is Resource.Success) updateCollections(resource.data)
            }
        }
    }

    private fun download() {
        if (uiState.value.isDownloading) return

        coroutineScope.launch {
            updateIsDownloading(true)

            val quality = observeUserSettings().first().downloadImageQuality

            photoRepository.trackDownload(photoId, quality).handleResponse(
                onSuccess = { url -> sendComponentEvent(ComponentEvent.OpenUrl(url)) },
                onError = { showRequestError() }
            )

            updateIsDownloading(false)
        }
    }

    private fun toggleCollection(collectionId: Int) {
        val state = uiState.value
        if (collectionId in state.pendingCollectionIds) return

        val isMember = collectionId in state.memberCollectionIds

        coroutineScope.launch {
            addPendingCollection(collectionId)

            val response = if (isMember) {
                collectionRepository.removePhotoFromCollection(collectionId, photoId)
            } else {
                collectionRepository.addPhotoToCollection(collectionId, photoId)
            }

            // On success the repository writes the change into the database and the photo flow delivers it.
            response.onError { showRequestError() }

            removePendingCollection(collectionId)
        }
    }

    private suspend fun showRequestError() {
        notifierManager.showLong(getString(Res.string.error_dialog_description))
    }

    private fun updateIsLoading(isLoading: Boolean) {
        uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun updateHasError(hasError: Boolean) {
        uiState.update { it.copy(hasError = hasError) }
    }

    /** The loaded photo also tells which collections of the user already hold it. */
    private fun showPhoto(photo: Photo) {
        uiState.update {
            it.copy(
                photo = photo,
                hasError = false,
                memberCollectionIds = photo.currentUserCollectionIds.toSet()
            )
        }
    }

    private fun updateStatistics(statistics: Statistics) {
        uiState.update { it.copy(statistics = statistics) }
    }

    private fun updateCollections(collections: List<PhotoCollection>) {
        uiState.update { it.copy(collections = collections) }
    }

    private fun updateIsDownloading(isDownloading: Boolean) {
        uiState.update { it.copy(isDownloading = isDownloading) }
    }

    private fun addPendingCollection(collectionId: Int) {
        uiState.update { it.copy(pendingCollectionIds = it.pendingCollectionIds + collectionId) }
    }

    private fun removePendingCollection(collectionId: Int) {
        uiState.update { it.copy(pendingCollectionIds = it.pendingCollectionIds - collectionId) }
    }
}

sealed interface PhotoNavigationEvent : BaseNavigationEvent {
    data class OpenProfile(val username: String) : PhotoNavigationEvent
}