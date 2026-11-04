package org.kimoteru.wallify.feature.profile.ui

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.domain.repository.CollectionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.profile.ui.ProfileContract.UiEvent
import org.kimoteru.wallify.feature.profile.ui.ProfileContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/** Whose profile the screen shows: the signed-in user's own (the studio tab) or another user's. */
sealed interface ProfileTarget {
    data object CurrentUser : ProfileTarget
    data class Username(val username: String) : ProfileTarget
}

interface ProfileComponent : BaseComponent<UiState, UiEvent> {
    /** Whether the screen was opened over another one and so has a back button. */
    val canNavigateBack: Boolean

    val photos: Flow<PagingData<Photo>>
    val collections: Flow<PagingData<PhotoCollection>>
    val collectionPhotos: Flow<PagingData<Photo>>
}

class ProfileDefaultComponent(
    componentContext: ComponentContext,
    target: ProfileTarget,
    navigateBack: (() -> Unit)?,
    private val openPhoto: (photoId: String) -> Unit
) : BaseDefaultComponent<ProfileLogic, UiState, UiEvent, ProfileNavigationEvent>(
    componentContext = componentContext,
    navigateBack = navigateBack
),
    ProfileComponent {

    override val logic: ProfileLogic = instanceKeeper.getOrCreate { get<ProfileLogic> { parametersOf(target) } }

    override val canNavigateBack: Boolean = navigateBack != null

    override val photos: Flow<PagingData<Photo>> = logic.photos

    override val collections: Flow<PagingData<PhotoCollection>> = logic.collections

    override val collectionPhotos: Flow<PagingData<Photo>> = logic.collectionPhotos

    override fun obtainNavigateEvent(event: ProfileNavigationEvent) {
        when (event) {
            is ProfileNavigationEvent.OpenPhoto -> openPhoto(event.photoId)
        }
    }
}

/**
 * The profile of one user with its photos and collections. The own profile waits for the session to publish the
 * signed-in user (`UserRepository.currentUser`) and then works like any other profile.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Factory
class ProfileLogic(
    @InjectedParam private val target: ProfileTarget,
    private val userRepository: UserRepository,
    private val collectionRepository: CollectionRepository
) : BaseLogic<UiState, UiEvent, ProfileNavigationEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    private val username: Flow<String> = when (target) {
        is ProfileTarget.CurrentUser -> userRepository.currentUser.filterNotNull().mapDistinctBy { it.username }
        is ProfileTarget.Username -> flowOf(target.username)
    }

    val photos: Flow<PagingData<Photo>> = username
        .flatMapLatest { login -> userRepository.getUserPhotosFlow(login) }
        .cachedIn(coroutineScope)

    val collections: Flow<PagingData<PhotoCollection>> = username
        .flatMapLatest { login -> userRepository.getUserCollectionsFlow(login) }
        .cachedIn(coroutineScope)

    val collectionPhotos: Flow<PagingData<Photo>> = uiState
        .mapDistinctBy { state -> state.openedCollection?.id }
        .flatMapLatest { id ->
            if (id == null) flowOf(PagingData.empty<Photo>()) else collectionRepository.getCollectionPhotosFlow(id)
        }
        .cachedIn(coroutineScope)

    private var loadJob: Job? = null

    override fun onCreate() = load()

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Retry -> load()
            is UiEvent.SelectTab -> updateSelectedTab(event.tab)
            is UiEvent.OpenCollection -> updateOpenedCollection(event.collection)
            is UiEvent.CloseCollection -> updateOpenedCollection(null)
            is UiEvent.OpenPhoto -> navigate(ProfileNavigationEvent.OpenPhoto(event.photoId))
        }
    }

    /** Follows the user and the statistics in the database: the saved ones first, then the backend's answer. */
    private fun load() {
        loadJob?.cancel()
        loadJob = coroutineScope.launch {
            updateIsLoading(true)

            val login = username.first()

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

    private fun updateIsLoading(isLoading: Boolean) {
        uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun updateHasError(hasError: Boolean) {
        uiState.update { it.copy(hasError = hasError) }
    }

    /** A loaded user also clears an earlier failure. */
    private fun showUser(user: User) {
        uiState.update { it.copy(user = user, hasError = false) }
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
}

sealed interface ProfileNavigationEvent : BaseNavigationEvent {
    data class OpenPhoto(val photoId: String) : ProfileNavigationEvent
}