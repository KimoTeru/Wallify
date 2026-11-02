package org.kimoteru.wallify.core.presentation.base

typealias BaseDefaultComponent<Logic, UiState, UiEvent, NavigationEvent> =
    BaseDefaultComponentCore<Logic, UiState, UiEvent, NavigationEvent, Nothing>

typealias BaseNavigationDefaultComponent<Child, Config, Logic, NavigateEvent> =
    BaseNavigationDefaultComponentCore<Child, Config, Logic, Nothing, Nothing, NavigateEvent, Nothing>

typealias BaseLogic<UiState, UiEvent, NavigateEvent> =
    BaseLogicCore<UiState, UiEvent, NavigateEvent, Nothing>

typealias BaseNavigationLogic<NavigateEvent> =
    BaseNavigationLogicCore<Nothing, Nothing, NavigateEvent, Nothing>

typealias BaseComponent<UiState, UiEvent> = BaseComponentCore<UiState, UiEvent, Nothing>

typealias BaseNavigationComponent<Child> = BaseNavigationComponentCore<Nothing, Nothing, Child, Nothing>