package org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain

import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent

interface ApiErrorManager {
    val events: StateEventFlowWithContent<ApiErrorManagerEvent>
    fun emitEvent(event: ApiErrorManagerEvent)
}