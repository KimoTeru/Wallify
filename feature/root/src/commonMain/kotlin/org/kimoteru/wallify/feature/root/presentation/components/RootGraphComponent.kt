package org.kimoteru.wallify.feature.root.presentation.components

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.combineState
import org.kimoteru.wallify.core.domain.ConnectivityRepository
import org.kimoteru.wallify.core.domain.SessionRepository
import org.kimoteru.wallify.core.presentation.base.BaseNavigationComponentCore
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.base.BaseNavigationLogicCore
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManager
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManagerEvent
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.getApiErrorManagerLogic
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.getNotifierManagerLogic
import org.kimoteru.wallify.feature.home.ui.HomeComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract.UiEvent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract.UiState
import org.kimoteru.wallify.feature.splash.ui.SplashComponent
import org.koin.core.annotation.Factory

interface RootGraphComponent : BaseNavigationComponentCore<UiState, UiEvent, RootGraphComponent.Child, Nothing> {
    sealed interface Child : BaseNavigationComponentCore.BaseNavigationChild {
        class Splash(val component: SplashComponent) : Child
        class Home(val component: HomeComponent) : Child
    }
}

/**
 * Root-level logic shared by every screen:
 * - hosts the API error dialog and the snackbar notifier,
 * - tracks internet availability while the app is in the foreground,
 * - clears the session when the backend reports that it expired.
 */
@Factory
class RootGraphLogic(
    private val connectivityRepository: ConnectivityRepository,
    private val sessionRepository: SessionRepository,
    private val apiErrorManager: ApiErrorManager,
    private val notifierManager: NotifierManager
) : BaseNavigationLogicCore<UiState, UiEvent, RootGraphNavigationEvent, Nothing>() {

    private val apiErrorManagerLogic = getApiErrorManagerLogic()
    private val notifierManagerLogic = getNotifierManagerLogic(coroutineScope)

    override val uiState: StateFlow<UiState> = combineState(
        coroutineScope,
        apiErrorManagerLogic.uiState,
        notifierManagerLogic.uiState,
        connectivityRepository.isOnline,
        initialValue = UiState()
    ) { apiErrorManagerState, notifierManagerState, isOnline ->
        UiState(
            apiErrorManagerState = apiErrorManagerState,
            notifierManagerState = notifierManagerState,
            isOnline = isOnline
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
                        is ApiErrorManagerEvent.ShowDialog -> apiErrorManagerLogic.showDialog()
                        is ApiErrorManagerEvent.HideDialog -> apiErrorManagerLogic.hideDialog()
                        is ApiErrorManagerEvent.SessionExpired -> sessionRepository.clear()
                    }
                }
            }

            launch {
                notifierManager.snackbarRequests.collectLatest { model ->
                    notifierManagerLogic.show(model)
                }
            }
        }
    }

    override fun onResume() = connectivityRepository.start()

    override fun onPause() = connectivityRepository.stop()
}

sealed interface RootGraphNavigationEvent : BaseNavigationEvent {
    data object Home : RootGraphNavigationEvent
}