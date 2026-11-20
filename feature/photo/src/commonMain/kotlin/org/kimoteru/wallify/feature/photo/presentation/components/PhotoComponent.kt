package org.kimoteru.wallify.feature.photo.presentation.components

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.extensions.toArgbOrNull
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.successData
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.collection.CollectionPhotoChange
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.errors.CollectionApiError
import org.kimoteru.wallify.core.presentation.photo.aspectRatioOrNull
import org.kimoteru.wallify.core.presentation.photo.forQuality
import org.kimoteru.wallify.core.presentation.photo.toAvatarUi
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.PhotoImageUi
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiEvent
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/** Proportions of a photo the backend sent no size for. */
private const val SQUARE_RATIO = 1f

interface PhotoComponent : BaseComponent<UiState, UiEvent>

/**
 * Loads the whole photo and the signed-in user's collections independently, so a failing extra never hides the photo.
 * The download goes through the browser: the logic reports the download to the backend and opens the address of the
 * file.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Factory
class PhotoLogic(
    @InjectedParam private val photoId: String,
    private val photoRepository: PhotoRepository,
    private val userRepository: UserRepository,
    private val collectionRepository: CollectionRepository,
    private val observeUserSettings: ObserveUserSettingsUseCase,
) : BaseLogic<UiState, UiEvent, PhotoNavigationEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    /** The photo can be added to the collections of the signed-in user; `null` without a session. */
    private val collectionsOwner: String? = userRepository.currentUser.value?.username

    /** Every change follows the photo in the database again, and the backend is asked again (a retry). */
    private val photoReloadKey = MutableStateFlow(0)

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                observeUserSettings()
                    .mapDistinctBy { settings -> settings.detailImageQuality to settings.listImageQuality }
                    .collect { (detailQuality, listQuality) -> updateImageQualities(detailQuality, listQuality) }
            }

            launch {
                photoReloadKey
                    .flatMapLatest { photoRepository.getPhotoFlow(photoId) }
                    .collect { resource ->
                        when (resource) {
                            is Resource.Loading -> showLoading()
                            is Resource.Success -> showPhoto(resource.data)
                            is Resource.Error -> showLoadError(resource.error)
                        }
                    }
            }

            collectionsOwner?.let { username ->
                launch {
                    userRepository.getUserCollectionListFlow(username)
                        .successData()
                        .collect { collections -> updateCollections(collections) }
                }
            }
        }
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Retry -> reloadPhoto()
            is UiEvent.Download -> download()
            is UiEvent.OpenSource -> uiState.value.photo?.htmlUrl?.let(intentNavigator::openUrl)
            is UiEvent.OpenAuthor -> uiState.value.photo?.user?.username?.let { username ->
                navigate(PhotoNavigationEvent.OpenProfile(username))
            }

            is UiEvent.ToggleCollection -> toggleCollection(event.collectionId)
        }
    }

    /** «Повторить»: `flatMapLatest` drops the failed subscription and follows the photo again. */
    private fun reloadPhoto() {
        photoReloadKey.update { it.inc() }
    }

    private fun download() {
        if (uiState.value.isDownloading) return

        coroutineScope.launch {
            updateIsDownloading(true)

            val quality = observeUserSettings().first().downloadImageQuality

            // The repeat goes through the button again, so it shows the progress too.
            handleResponseDefault(
                request = { photoRepository.trackDownload(photoId, quality) },
                onSuccess = { url -> intentNavigator.openUrl(url) },
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

    /** The photo is asked for again: the failure of the last try is over. */
    private fun showLoading() {
        uiState.update { it.copy(isLoading = true, error = null) }
    }

    /** Nothing is saved and the backend failed: the failure takes the screen. */
    private fun showLoadError(error: ResponseError) {
        uiState.update { it.copy(isLoading = false, error = error) }
    }

    /**
     * The loaded photo also tells which collections of the user already hold it, and gives the screen its image and
     * the avatar of the author ready to draw.
     */
    private fun showPhoto(photo: Photo) {
        uiState.update {
            it.copy(
                photo = photo,
                image = photo.toImageUi(detailQuality = it.detailImageQuality, listQuality = it.listImageQuality),
                author = photo.user?.let { user -> user.toAvatarUi(imageUrl = user.profileImage?.medium) },
                isLoading = false,
                error = null,
                memberCollectionIds = photo.currentUserCollectionIds.toSet()
            )
        }
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

    /**
     * Both qualities come from the settings together: the list one stands in while the detail one loads. The image
     * of a shown photo follows them.
     */
    private fun updateImageQualities(detailQuality: ImageQuality, listQuality: ImageQuality) {
        uiState.update {
            it.copy(
                detailImageQuality = detailQuality,
                listImageQuality = listQuality,
                image = it.photo?.toImageUi(detailQuality = detailQuality, listQuality = listQuality)
            )
        }
    }

    /** The image of the screen: its color, the small version, the chosen one and the one from the list. */
    private fun Photo.toImageUi(detailQuality: ImageQuality, listQuality: ImageQuality): PhotoImageUi = PhotoImageUi(
        lowUrl = urls.forQuality(ImageQuality.Low),
        highUrl = urls.forQuality(detailQuality),
        placeholderKey = urls.forQuality(listQuality),
        colorArgb = color.toArgbOrNull(),
        aspectRatio = aspectRatioOrNull() ?: SQUARE_RATIO
    )
}

sealed interface PhotoNavigationEvent : BaseNavigationEvent {
    data class OpenProfile(val username: String) : PhotoNavigationEvent
}