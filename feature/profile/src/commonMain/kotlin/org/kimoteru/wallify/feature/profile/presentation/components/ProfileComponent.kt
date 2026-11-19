package org.kimoteru.wallify.feature.profile.presentation.components

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.domain.photo.forQuality
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseLogicCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.errors.ProfileApiError
import org.kimoteru.wallify.core.presentation.photo.PhotoCardUi
import org.kimoteru.wallify.core.presentation.photo.profileSubtitle
import org.kimoteru.wallify.core.presentation.photo.toAvatarUi
import org.kimoteru.wallify.core.presentation.photo.toCardUi
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.CollectionCardUi
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.ComponentEvent
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.EditField
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.EditForm
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.UiEvent
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/** Whose profile the screen shows: the signed-in user's own (the studio tab) or another user's. */
sealed interface ProfileTarget {
    data object CurrentUser : ProfileTarget
    data class Username(val username: String) : ProfileTarget
}

interface ProfileComponent : BaseComponentCore<UiState, UiEvent, ComponentEvent> {
    /** Whether the screen was opened over another one and so has a back button. */
    val canNavigateBack: Boolean

    val photos: Flow<PagingData<PhotoCardUi>>
    val collections: Flow<PagingData<CollectionCardUi>>
    val collectionPhotos: Flow<PagingData<PhotoCardUi>>
}

/**
 * The profile of one user with its photos and collections. The own profile waits for the session to publish the
 * signed-in user (`UserRepository.currentUser`) and then works like any other profile; it also edits the profile
 * and sends the user to the photo service's page to upload photos (the API cannot upload), then reloads the photos
 * when the user is back.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Factory
class ProfileLogic(
    @InjectedParam private val target: ProfileTarget,
    private val userRepository: UserRepository,
    private val collectionRepository: CollectionRepository,
    private val observeUserSettings: ObserveUserSettingsUseCase,
) : BaseLogicCore<UiState, UiEvent, ProfileNavigationEvent, ComponentEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    private val username: Flow<String> = when (target) {
        is ProfileTarget.CurrentUser -> userRepository.currentUser.filterNotNull().mapDistinctBy { it.username }
        is ProfileTarget.Username -> flowOf(target.username)
    }

    /** Every change builds the photo list again, which loads it from the backend if it was made stale. */
    private val photosReloadKey = MutableStateFlow(0)

    private val listImageQuality: Flow<ImageQuality> = uiState.mapDistinctBy { state -> state.listImageQuality }

    // The lists give ready cards to the Ui. The quality is joined after `cachedIn`, which may be collected again.
    val photos: Flow<PagingData<PhotoCardUi>> = combine(username, photosReloadKey) { login, _ -> login }
        .flatMapLatest { login -> userRepository.getUserPhotosFlow(login) }
        .cachedIn(coroutineScope)
        .combine(listImageQuality) { photos, quality -> photos.map { photo -> photo.toCard(quality) } }

    val collections: Flow<PagingData<CollectionCardUi>> = username
        .flatMapLatest { login -> userRepository.getUserCollectionsFlow(login) }
        .cachedIn(coroutineScope)
        .combine(listImageQuality) { collections, quality ->
            collections.map { collection ->
                CollectionCardUi(collection = collection, coverUrl = collection.coverPhoto?.urls?.forQuality(quality))
            }
        }

    val collectionPhotos: Flow<PagingData<PhotoCardUi>> = uiState
        .mapDistinctBy { state -> state.openedCollection?.id }
        .flatMapLatest { id ->
            if (id == null) flowOf(PagingData.empty()) else collectionRepository.getCollectionPhotosFlow(id)
        }
        .cachedIn(coroutineScope)
        .combine(listImageQuality) { photos, quality ->
            photos.map { photo -> photo.toCard(quality) }
        }

    private var loadJob: Job? = null

    private var isUploadPending = false

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                observeUserSettings()
                    .mapDistinctBy { settings -> settings.listImageQuality }
                    .collect { quality -> updateListImageQuality(quality) }
            }
        }

        showSavedUser()
        load()
    }

    /** The user comes back from the browser after the upload page: the new photos and the counters are loaded. */
    override fun onResume() {
        if (!isUploadPending) return
        isUploadPending = false
        reloadAfterUpload()
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Retry -> load()
            is UiEvent.SelectTab -> updateSelectedTab(event.tab)
            is UiEvent.OpenCollection -> updateOpenedCollection(event.collection)
            is UiEvent.CloseCollection -> updateOpenedCollection(null)
            is UiEvent.OpenPhoto -> navigate(ProfileNavigationEvent.OpenPhoto(event.photoId))
            is UiEvent.Upload -> upload()
            is UiEvent.ShowEdit -> showEdit()
            is UiEvent.HideEdit -> updateEditForm(null)
            is UiEvent.ChangeEditField -> changeEditField(event.field, event.value)
            is UiEvent.SaveEdit -> saveEdit()
            is UiEvent.ListRefreshError -> showListRefreshError(event.list, event.error) { addListRetry() }
        }
    }

    /** The own profile opens with the saved signed-in user, so it is never empty while the backend answers. */
    private fun showSavedUser() {
        val saved = userRepository.currentUser.value.takeIf { target is ProfileTarget.CurrentUser } ?: return
        showUser(saved)
        updateIsLoading(false)
    }

    /**
     * Follows the user and the statistics in the database: the saved ones first, then the backend's answer. The own
     * profile starts over when the username changes.
     */
    private fun load() {
        loadJob?.cancel()
        loadJob = coroutineScope.launch {
            if (uiState.value.user == null) updateIsLoading(true)

            username.collectLatest { login ->
                coroutineScope {
                    launch {
                        userRepository.getUserStatisticsFlow(login).collect { resource ->
                            if (resource is Resource.Success) updateStatistics(resource.data)
                        }
                    }

                    userRepository.getUserFlow(login).collect { resource ->
                        when (resource) {
                            is Resource.Loading -> updateIsLoading(true)
                            is Resource.Success -> {
                                showUser(resource.data)
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
        }
    }

    private fun upload() {
        isUploadPending = true
        sendComponentEvent(ComponentEvent.OpenUrl(userRepository.uploadPageUrl))
    }

    private fun reloadAfterUpload() {
        coroutineScope.launch {
            userRepository.invalidateUserPhotos(username.first())
            photosReloadKey.update { it + 1 }
            load()
        }
    }

    private fun showEdit() {
        val user = uiState.value.user ?: return
        updateEditForm(EditForm.of(user))
    }

    private fun changeEditField(field: EditField, value: String) {
        val form = uiState.value.editForm ?: return
        updateEditForm(form.with(field, value))
    }

    private fun saveEdit() {
        val state = uiState.value
        val form = state.editForm ?: return
        val user = state.user ?: return
        if (state.isSaving || !form.isValid) return

        coroutineScope.launch {
            updateIsSaving(true)

            // A failure keeps the form open with what was typed; the snackbar over it names the failure, and its
            // repeat saves the form as it is then.
            handleResponse<User, ProfileApiError>(
                request = { userRepository.updateCurrentUser(form.changesTo(user)) },
                onSuccess = { updateEditForm(null) },
                onRetry = { saveEdit() }
            )

            updateIsSaving(false)
        }
    }

    private fun updateIsLoading(isLoading: Boolean) {
        uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun updateError(error: ResponseError?) {
        uiState.update { it.copy(error = error) }
    }

    /**
     * A loaded user also clears an earlier failure, tells whether the profile is the signed-in user's own and gives
     * the header its avatar and subtitle.
     */
    private fun showUser(user: User) {
        val isOwn = target is ProfileTarget.CurrentUser || user.id == userRepository.currentUser.value?.id
        uiState.update {
            it.copy(
                user = user,
                avatar = user.toAvatarUi(imageUrl = user.profileImage?.large),
                userSubtitle = user.profileSubtitle(),
                isOwn = isOwn,
                error = null
            )
        }
    }

    private fun updateStatistics(statistics: Statistics) {
        uiState.update { it.copy(statistics = statistics) }
    }

    private fun updateSelectedTab(tab: ProfileContract.Tab) {
        uiState.update { it.copy(selectedTab = tab) }
    }

    private fun updateOpenedCollection(collection: PhotoCollection?) {
        uiState.update { it.copy(openedCollection = collection) }
    }

    private fun updateEditForm(form: EditForm?) {
        uiState.update { it.copy(editForm = form) }
    }

    private fun updateIsSaving(isSaving: Boolean) {
        uiState.update { it.copy(isSaving = isSaving) }
    }

    private fun updateListImageQuality(quality: ImageQuality) {
        uiState.update { it.copy(listImageQuality = quality) }
    }

    /** «Повторить» on a failed refresh of the list of the tab: it repeats itself when the count grows. */
    private fun addListRetry() {
        uiState.update { it.copy(listRetries = it.listRetries + 1) }
    }

    /** All photos of a profile are the same author's, so the cards show none. */
    private fun Photo.toCard(quality: ImageQuality): PhotoCardUi = toCardUi(
        imageUrl = urls.forQuality(quality),
        lowImageUrl = urls.forQuality(ImageQuality.Low),
        withAuthor = false
    )
}

sealed interface ProfileNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : ProfileNavigationEvent
}