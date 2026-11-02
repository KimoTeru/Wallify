package org.kimoteru.wallify.feature.splash.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseDefaultComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.splash.ui.SplashContract.UiEvent
import org.koin.core.annotation.Factory
import org.koin.core.component.get

interface SplashComponent : BaseComponent<SplashContract.UiState, UiEvent>

class SplashDefaultComponent(
    componentContext: ComponentContext,
    private val navigateNext: () -> Unit
) : BaseDefaultComponent<SplashLogic, SplashContract.UiState, UiEvent, SplashNavigationEvent>(
    componentContext = componentContext
),
    SplashComponent {

    override val logic: SplashLogic = instanceKeeper.getOrCreate { get<SplashLogic>() }

    override fun obtainNavigateEvent(event: SplashNavigationEvent) {
        when (event) {
            is SplashNavigationEvent.Next -> navigateNext()
        }
    }
}

@Factory
class SplashLogic : BaseLogic<SplashContract.UiState, UiEvent, SplashNavigationEvent>() {

    override val uiState: StateFlow<SplashContract.UiState>
        field = MutableStateFlow(SplashContract.UiState())

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.EndAnimation -> navigate(SplashNavigationEvent.Next)
        }
    }
}

sealed interface SplashNavigationEvent : BaseNavigationEvent {
    data object Next : SplashNavigationEvent
}