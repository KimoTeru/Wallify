package org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain

sealed interface ApiErrorManagerEvent {
    data object ShowDialog : ApiErrorManagerEvent
    data object HideDialog : ApiErrorManagerEvent
    data object SessionExpired : ApiErrorManagerEvent
}