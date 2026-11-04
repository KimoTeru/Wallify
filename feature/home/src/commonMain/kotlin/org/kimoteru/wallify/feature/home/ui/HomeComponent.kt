package org.kimoteru.wallify.feature.home.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.combineState
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.koin.core.annotation.Factory
import org.koin.core.component.get

interface HomeComponent : BaseComponent<HomeContract.UiState, HomeContract.UiEvent>

class HomeDefaultComponent(
    componentContext: ComponentContext
) : BaseDefaultComponent<HomeLogic, HomeContract.UiState, HomeContract.UiEvent, HomeNavigationEvent>(
    componentContext = componentContext
),
    HomeComponent {

    override val logic: HomeLogic = instanceKeeper.getOrCreate { get<HomeLogic>() }

    override fun obtainNavigateEvent(event: HomeNavigationEvent) = Unit
}

/**
 * Only reads what the session keeps ([UserRepository.currentUser]); loading the user is not the screen's job.
 * Signing out needs no navigation here: the root graph sees the missing session and shows the sign-in screen.
 */
@Factory
class HomeLogic(
    private val authRepository: AuthRepository,
    userRepository: UserRepository
) : BaseLogic<HomeContract.UiState, HomeContract.UiEvent, HomeNavigationEvent>() {

    override val uiState: StateFlow<HomeContract.UiState> = combineState(
        coroutineScope,
        userRepository.currentUser,
        initialValue = HomeContract.UiState(userName = userRepository.currentUser.value?.name)
    ) { user ->
        HomeContract.UiState(userName = user?.name)
    }

    override fun obtainUiEvent(event: HomeContract.UiEvent) {
        when (event) {
            is HomeContract.UiEvent.SignOut -> coroutineScope.launch { authRepository.signOut() }
        }
    }
}

sealed interface HomeNavigationEvent : BaseNavigationEvent