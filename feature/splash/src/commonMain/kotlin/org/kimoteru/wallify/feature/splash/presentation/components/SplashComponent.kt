package org.kimoteru.wallify.feature.splash.presentation.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.splash.presentation.ui.SplashContract
import org.kimoteru.wallify.feature.splash.presentation.ui.SplashContract.UiEvent
import org.koin.core.annotation.Factory

interface SplashComponent : BaseComponent<SplashContract.UiState, UiEvent>

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