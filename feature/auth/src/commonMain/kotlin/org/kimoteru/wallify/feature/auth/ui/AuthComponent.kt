package org.kimoteru.wallify.feature.auth.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.response.onError
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManager
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManagerEvent
import org.kimoteru.wallify.feature.auth.ui.AuthContract.UiEvent
import org.koin.core.annotation.Factory
import org.koin.core.component.get

interface AuthComponent : BaseComponent<AuthContract.UiState, UiEvent>

class AuthDefaultComponent(
    componentContext: ComponentContext
) : BaseDefaultComponent<AuthLogic, AuthContract.UiState, UiEvent, AuthNavigationEvent>(
    componentContext = componentContext
),
    AuthComponent {

    override val logic: AuthLogic = instanceKeeper.getOrCreate { get<AuthLogic>() }

    override fun obtainNavigateEvent(event: AuthNavigationEvent) = Unit
}

/**
 * Starts the sign-in in the browser and shows that it is waiting. The screen never leaves by itself: once the
 * token is kept, the root graph sees the signed-in user and replaces this screen (`code/navigation.md`).
 */
@Factory
class AuthLogic(
    private val authRepository: AuthRepository,
    private val apiErrorManager: ApiErrorManager
) : BaseLogic<AuthContract.UiState, UiEvent, AuthNavigationEvent>() {

    private var signInJob: Job? = null

    override val uiState: StateFlow<AuthContract.UiState>
        field = MutableStateFlow(AuthContract.UiState())

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.SignIn -> signIn()
            is UiEvent.CancelSignIn -> signInJob?.cancel()
        }
    }

    private fun signIn() {
        if (signInJob?.isActive == true) return

        signInJob = coroutineScope.launch {
            updateIsSigningIn(true)

            try {
                authRepository.signIn()
                    .onError { apiErrorManager.emitEvent(ApiErrorManagerEvent.ShowDialog) }
            } finally {
                updateIsSigningIn(false)
            }
        }
    }

    private fun updateIsSigningIn(isSigningIn: Boolean) {
        uiState.update { it.copy(isSigningIn = isSigningIn) }
    }
}

sealed interface AuthNavigationEvent : BaseNavigationEvent