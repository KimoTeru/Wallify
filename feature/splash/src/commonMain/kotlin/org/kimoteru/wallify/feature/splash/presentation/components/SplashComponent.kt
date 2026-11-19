package org.kimoteru.wallify.feature.splash.presentation.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.presentation.base.BaseComponent
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.feature.splash.presentation.ui.SplashContract.UiEvent
import org.kimoteru.wallify.feature.splash.presentation.ui.SplashContract.UiState
import org.koin.core.annotation.Factory

interface SplashComponent : BaseComponent<UiState, UiEvent>

@Factory
class SplashLogic : BaseLogic<UiState, UiEvent, SplashNavigationEvent>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.EndAnimation -> navigate(SplashNavigationEvent.Next)
        }
    }
}

sealed interface SplashNavigationEvent : BaseNavigationEvent {
    data object Next : SplashNavigationEvent
}