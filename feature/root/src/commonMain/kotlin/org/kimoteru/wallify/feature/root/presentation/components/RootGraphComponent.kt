package org.kimoteru.wallify.feature.root.presentation.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.kimoteru.wallify.core.common.flow.combineState
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.domain.manager.UserSessionManager
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.usecase.invoke
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.presentation.base.BaseNavigationComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.base.BaseNavigationLogicCore
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.getNotifierManagerLogic
import org.kimoteru.wallify.core.presentation.settings.toPresentation
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_dialog_description
import org.kimoteru.wallify.core.resources.generated.resources.error_no_internet
import org.kimoteru.wallify.feature.auth.presentation.components.AuthComponent
import org.kimoteru.wallify.feature.photo.presentation.components.PhotoComponent
import org.kimoteru.wallify.feature.profile.presentation.components.ProfileComponent
import org.kimoteru.wallify.feature.main.presentation.components.MainGraphComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract.UiEvent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract.UiState
import org.kimoteru.wallify.feature.splash.presentation.components.SplashComponent
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
 * - hosts the snackbar: no internet while the device is offline, and a failed load of the signed-in user while it is online,
 * - tracks internet availability while the app is in the foreground,
 * - passes the color scheme, the screen layout and the photo layout chosen in the settings to the apps and
 *   screens,
 * - guards the app: after the splash the user without a session sees the sign-in screen, the signed-in user sees
 *   the main screens, and the screen follows every later change of the session (sign-in, sign-out, expired token),
 * - tells the user when the signed-in user's data could not be loaded. Offline needs no extra notice: the snackbar
 *   is already up, and the data is loaded again when the connection returns.
 */
@Factory
class RootGraphLogic(
    private val connectivityRepository: ConnectivityRepository,
    private val authRepository: AuthRepository,
    private val userSessionManager: UserSessionManager,
    private val notifierManager: NotifierManager,
    observeUserSettings: ObserveUserSettingsUseCase
) : BaseNavigationLogicCore<UiState, UiEvent, RootGraphNavigationEvent, Nothing>() {

    private val isSplashFinished = MutableStateFlow(false)

    private val notifierManagerLogic = getNotifierManagerLogic(coroutineScope)

    /** The snackbar on screen is the no-internet one, so coming back online may dismiss it. */
    private var isOfflineNotice = false

    override val uiState: StateFlow<UiState> = combineState(
        coroutineScope,
        notifierManagerLogic.uiState,
        connectivityRepository.isOnline,
        observeUserSettings().mapDistinctBy { settings -> settings },
        initialValue = UiState()
    ) { notifierManagerState, isOnline, settings ->
        UiState(
            notifierManagerState = notifierManagerState,
            isOnline = isOnline,
            themeMode = settings.themeMode.toPresentation(),
            photoLayout = settings.photoLayout.toPresentation(),
            photoColumns = settings.photoColumns,
            screenLayout = settings.screenLayout.toPresentation(),
            languageTag = settings.languageTag,
            isSettingsLoaded = true
        )
    }

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.NotifierManagerEvent -> notifierManagerLogic.obtainUiEvent(event.event)
        }
    }

    override fun onCreate() {
        coroutineScope.launch {
            launch {
                notifierManager.snackbarRequests.collectLatest { model ->
                    // Offline already shows its own snackbar, so a failed action does not add a second one.
                    if (connectivityRepository.isOnline.value) notifierManagerLogic.show(model)
                }
            }

            launch {
                connectivityRepository.isOnline.collectLatest { isOnline ->
                    if (isOnline) {
                        if (isOfflineNotice) {
                            isOfflineNotice = false
                            notifierManagerLogic.dismiss()
                        }
                    } else {
                        isOfflineNotice = true
                        notifierManagerLogic.show(
                            NotifierModel(
                                message = getString(Res.string.error_no_internet),
                                snackbarDuration = SnackbarDuration.Indefinite
                            )
                        )
                    }
                }
            }

            launch {
                userSessionManager.loadError.collectLatest { error ->
                    if (error != null) notifierManager.showLong(getString(Res.string.error_dialog_description))
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