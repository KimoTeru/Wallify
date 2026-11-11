package org.kimoteru.wallify.feature.photo.presentation.components

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.presentation.base.BaseComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseLogicCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.errors.CollectionApiError
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
    private val observeUserSettings: ObserveUserSettingsUseCase,
) : BaseLogicCore<UiState, UiEvent, PhotoNavigationEvent, ComponentEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    private var photoJob: Job? = null

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                observeUserSettings()
                    .mapDistinctBy { settings -> settings.detailImageQuality to settings.listImageQuality }
                    .collect { (detailQuality, listQuality) -> updateImageQualities(detailQuality, listQuality) }
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
                        updateError(null)
                        updateIsLoading(true)
                    }
                    is Resource.Success -> {
                        showPhoto(resource.data)
                        updateIsLoading(false)
                    }
                    is Resource.Error -> {
                        updateError(resource.error)
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

            // The repeat goes through the button again, so it shows the progress too.
            handleResponseDefault(
                request = { photoRepository.trackDownload(photoId, quality) },
                onSuccess = { url -> sendComponentEvent(ComponentEvent.OpenUrl(url)) },
                onRetry = { download() }
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

            // On success the repository writes the change into the database and the photo flow delivers it. The
            // repeat is a new press on the collection: it reads the membership again and shows the progress.
            handleResponse<CollectionPhotoChange, CollectionApiError>(
                request = {
                    if (isMember) {
                        collectionRepository.removePhotoFromCollection(collectionId, photoId)
                    } else {
                        collectionRepository.addPhotoToCollection(collectionId, photoId)
                    }
                },
                onRetry = { toggleCollection(collectionId) }
            )

            removePendingCollection(collectionId)
        }
    }

    private fun updateIsLoading(isLoading: Boolean) {
        uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun updateError(error: ResponseError?) {
        uiState.update { it.copy(error = error) }
    }

    /** The loaded photo also tells which collections of the user already hold it. */
    private fun showPhoto(photo: Photo) {
        uiState.update {
            it.copy(
                photo = photo,
                error = null,
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

    /** Both qualities come from the settings together: the list one stands in while the detail one loads. */
    private fun updateImageQualities(detailQuality: ImageQuality, listQuality: ImageQuality) {
        uiState.update { it.copy(detailImageQuality = detailQuality, listImageQuality = listQuality) }
    }
}

sealed interface PhotoNavigationEvent : BaseNavigationEvent {
    data class OpenProfile(val username: String) : PhotoNavigationEvent
}