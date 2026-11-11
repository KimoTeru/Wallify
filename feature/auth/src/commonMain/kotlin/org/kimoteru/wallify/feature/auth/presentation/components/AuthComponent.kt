package org.kimoteru.wallify.feature.auth.presentation.components

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.errors.AuthApiError
import org.kimoteru.wallify.feature.auth.presentation.ui.AuthContract
import org.kimoteru.wallify.feature.auth.presentation.ui.AuthContract.UiEvent
import org.koin.core.annotation.Factory

interface AuthComponent : BaseComponent<AuthContract.UiState, UiEvent>

/**
 * Starts the sign-in in the browser and shows that it is waiting. The screen never leaves by itself: once the
 * token is kept, the root graph sees the signed-in user and replaces this screen (`code/navigation.md`).
 */
@Factory
class AuthLogic(
    private val authRepository: AuthRepository,
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
                // The repeat is a new sign-in with a new browser page: the code of the failed one is already used.
                handleResponse<Boolean, AuthApiError>(
                    request = { authRepository.signIn() },
                    onRetry = { signIn() }
                )
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