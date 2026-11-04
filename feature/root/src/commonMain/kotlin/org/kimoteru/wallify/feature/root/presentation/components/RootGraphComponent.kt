package org.kimoteru.wallify.feature.root.presentation.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.combineState
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.domain.manager.UserSessionManager
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.presentation.base.BaseNavigationComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.base.BaseNavigationLogicCore
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManager
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManagerEvent
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.getApiErrorManagerLogic
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.getNotifierManagerLogic
import org.kimoteru.wallify.feature.auth.ui.AuthComponent
import org.kimoteru.wallify.feature.photo.ui.PhotoComponent
import org.kimoteru.wallify.feature.profile.ui.ProfileComponent
import org.kimoteru.wallify.feature.root.main.MainGraphComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract.UiEvent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract.UiState
import org.kimoteru.wallify.feature.splash.ui.SplashComponent
import org.koin.core.annotation.Factory

interface RootGraphComponent : BaseNavigationComponentCore<UiState, UiEvent, RootGraphComponent.Child, Nothing> {
    sealed interface Child : BaseNavigationComponentCore.BaseNavigationChild {
        class Splash(val component: SplashComponent) : Child
        class Main(val component: MainGraphComponent) : Child
        class Photo(val component: PhotoComponent) : Child
        class Profile(val component: ProfileComponent) : Child
        class Auth(val component: AuthComponent) : Child
    }
}

/**
 * Root-level logic shared by every screen:
 * - hosts the API error dialog and the snackbar notifier,
 * - tracks internet availability while the app is in the foreground,
 * - clears the session when the backend reports that it expired,
 * - passes the color scheme, the screen layout and the photo layout chosen in the settings to the apps and
 *   screens,
 * - guards the app: after the splash the user without a session sees the sign-in screen, the signed-in user sees
 *   the main screens, and the screen follows every later change of the session (sign-in, sign-out, expired token),
 * - tells the user when the signed-in user's data could not be loaded. Offline needs no dialog: the banner is shown
 *   and the data is loaded again when the connection returns. A dialog that a failed action opened while offline
 *   talks about the internet and closes by itself when the connection returns.
 */
@Factory
class RootGraphLogic(
    private val connectivityRepository: ConnectivityRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager,
    private val apiErrorManager: ApiErrorManager,
    private val notifierManager: NotifierManager,
    observeUserSettings: ObserveUserSettingsUseCase
) : BaseNavigationLogicCore<UiState, UiEvent, RootGraphNavigationEvent, Nothing>() {

    private val isSplashFinished = MutableStateFlow(false)

    private var isDialogForOffline = false

    private val apiErrorManagerLogic = getApiErrorManagerLogic()
    private val notifierManagerLogic = getNotifierManagerLogic(coroutineScope)

    override val uiState: StateFlow<UiState> = combineState(
        coroutineScope,
        apiErrorManagerLogic.uiState,
        notifierManagerLogic.uiState,
        connectivityRepository.isOnline,
        observeUserSettings().mapDistinctBy { settings -> settings },
        initialValue = UiState()
    ) { apiErrorManagerState, notifierManagerState, isOnline, settings ->
        UiState(
            apiErrorManagerState = apiErrorManagerState,
            notifierManagerState = notifierManagerState,
            isOnline = isOnline,
            themeMode = settings.themeMode,
            photoLayout = settings.photoLayout,
            photoColumns = settings.photoColumns,
            screenLayout = settings.screenLayout,
            isSettingsLoaded = true
        )
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ApiErrorManagerEvent -> apiErrorManagerLogic.obtainUiEvent(event.event)
            is UiEvent.NotifierManagerEvent -> notifierManagerLogic.obtainUiEvent(event.event)
        }
    }

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                apiErrorManager.events.collectLatest { event ->
                    when (event) {
                        is ApiErrorManagerEvent.ShowDialog -> {
                            isDialogForOffline = !connectivityRepository.isOnline.value
                            apiErrorManagerLogic.showDialog()
                        }
                        is ApiErrorManagerEvent.HideDialog -> apiErrorManagerLogic.hideDialog()
                        is ApiErrorManagerEvent.SessionExpired -> sessionRepository.clear()
                    }
                }
            }

            // A dialog about the missing internet has no point once the connection is back: it closes by itself.
            launch {
                connectivityRepository.isOnline.collect { isOnline ->
                    if (isOnline && isDialogForOffline) {
                        isDialogForOffline = false
                        apiErrorManagerLogic.hideDialog()
                    }
                }
            }

            launch {
                notifierManager.snackbarRequests.collectLatest { model ->
                    notifierManagerLogic.show(model)
                }
            }

            launch {
                userSessionManager.loadError.collectLatest { error ->
                    if (error != null && connectivityRepository.isOnline.value) {
                        apiErrorManagerLogic.showDialog()
                    }
                }
            }

            launch {
                isSplashFinished.first { it }

                authRepository.isSignedIn.collectLatest { isSignedIn ->
                    navigate(if (isSignedIn) RootGraphNavigationEvent.Main else RootGraphNavigationEvent.Auth)
                }
            }
        }
    }

    /** The splash is over (or the app was restored past it): from now on the session decides the screen. */
    fun onSplashFinished() {
        isSplashFinished.value = true
    }

    override fun onResume() = connectivityRepository.start()

    override fun onPause() = connectivityRepository.stop()
}

sealed interface RootGraphNavigationEvent : BaseNavigationEvent {
    data object Main : RootGraphNavigationEvent
    data object Auth : RootGraphNavigationEvent
}