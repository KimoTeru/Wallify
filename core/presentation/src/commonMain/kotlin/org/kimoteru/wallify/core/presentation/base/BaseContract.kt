package org.kimoteru.wallify.core.presentation.base

interface BaseContract {
    interface UiState

    data class BaseUiState(
        val isLoading: Boolean = false
    )

    interface UiEvent {
        data object NavigateBack : UiEvent
    }

    interface ComponentEvent
}

interface BaseNavigationEvent