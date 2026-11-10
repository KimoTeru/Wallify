package org.kimoteru.wallify.feature.profile.presentation.components

import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import org.kimoteru.wallify.core.common.response.handleResponse
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseLogicCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract
import org.kimoteru.wallify.feature.profile.presentation.ui.EditField
import org.kimoteru.wallify.feature.profile.presentation.ui.EditForm
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract.ComponentEvent
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

    val photos: Flow<PagingData<Photo>>
    val collections: Flow<PagingData<PhotoCollection>>
    val collectionPhotos: Flow<PagingData<Photo>>
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
    private val observeUserSettings: ObserveUserSettingsUseCase
) : BaseLogicCore<UiState, UiEvent, ProfileNavigationEvent, ComponentEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    private val username: Flow<String> = when (target) {
        is ProfileTarget.CurrentUser -> userRepository.currentUser.filterNotNull().mapDistinctBy { it.username }
        is ProfileTarget.Username -> flowOf(target.username)
    }

    /** Every change builds the photo list again, which loads it from the backend if it was made stale. */
    private val photosReloadKey = MutableStateFlow(0)

    val photos: Flow<PagingData<Photo>> = combine(username, photosReloadKey) { login, _ -> login }
        .flatMapLatest { login -> userRepository.getUserPhotosFlow(login) }
        .cachedIn(coroutineScope)

    val collections: Flow<PagingData<PhotoCollection>> = username
        .flatMapLatest { login -> userRepository.getUserCollectionsFlow(login) }
        .cachedIn(coroutineScope)

    val collectionPhotos: Flow<PagingData<Photo>> = uiState
        .mapDistinctBy { state -> state.openedCollection?.id }
        .flatMapLatest { id ->
            if (id == null) flowOf(PagingData.empty()) else collectionRepository.getCollectionPhotosFlow(id)
        }
        .cachedIn(coroutineScope)

    private var loadJob: Job? = null

    private var isUploadPending = false

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                observeUserSettings()
                    .mapDistinctBy { settings -> settings.listImageQuality }
                    .collect { quality -> uiState.update { it.copy(listImageQuality = quality) } }
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
                                updateHasError(true)
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
        updateHasSaveError(false)
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
            updateHasSaveError(false)

            userRepository.updateCurrentUser(form.changesTo(user)).handleResponse(
                onSuccess = { updateEditForm(null) },
                onError = { updateHasSaveError(true) }
            )

            updateIsSaving(false)
        }
    }

    private fun updateIsLoading(isLoading: Boolean) {
        uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun updateHasError(hasError: Boolean) {
        uiState.update { it.copy(hasError = hasError) }
    }

    /** A loaded user also clears an earlier failure and tells whether the profile is the signed-in user's own. */
    private fun showUser(user: User) {
        val isOwn = target is ProfileTarget.CurrentUser || user.id == userRepository.currentUser.value?.id
        uiState.update { it.copy(user = user, isOwn = isOwn, hasError = false) }
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

    private fun updateHasSaveError(hasSaveError: Boolean) {
        uiState.update { it.copy(hasSaveError = hasSaveError) }
    }
}

sealed interface ProfileNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : ProfileNavigationEvent
}