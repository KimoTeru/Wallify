package org.kimoteru.wallify.core.presentation.components.description_clickable_text.presentation.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kimoteru.wallify.core.presentation.base.BaseComponentLogic
import org.kimoteru.wallify.core.presentation.base.BaseNavigationEvent
import org.kimoteru.wallify.core.presentation.components.description_clickable_text.presentation.ui.DescriptionClickableTextContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.description_clickable_text.presentation.ui.DescriptionClickableTextContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

fun KoinComponent.getDescriptionClickableTextLogic(): DescriptionClickableTextLogic {
    return get { parametersOf() }
}

@Factory
class DescriptionClickableTextLogic :
    BaseComponentLogic<UiState, UiEvent, DescriptionClickableTextNavigationEvent>() {

    private val _uiState = MutableStateFlow(UiState())
    override val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.NavigateOnPrivacyPolicy -> navigate(
                DescriptionClickableTextNavigationEvent.PrivacyPolicy
            )

            is UiEvent.NavigateOnTermsOfService -> navigate(
                DescriptionClickableTextNavigationEvent.TermsOfService
            )

            is UiEvent.NavigateOnSubscriptionPolicy -> navigate(
                DescriptionClickableTextNavigationEvent.SubscriptionPolicy
            )
        }
    }
}

sealed interface DescriptionClickableTextNavigationEvent : BaseNavigationEvent {
    data object PrivacyPolicy : DescriptionClickableTextNavigationEvent
    data object TermsOfService : DescriptionClickableTextNavigationEvent
    data object SubscriptionPolicy : DescriptionClickableTextNavigationEvent
}