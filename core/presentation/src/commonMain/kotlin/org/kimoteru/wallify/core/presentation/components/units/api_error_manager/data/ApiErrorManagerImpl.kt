package org.kimoteru.wallify.core.presentation.components.units.api_error_manager.data

import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManager
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManagerEvent
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.asStateEventWithContentFlow
import org.koin.core.annotation.Singleton

@Singleton
class ApiErrorManagerImpl : ApiErrorManager {
    private val _events = MutableStateEventFlowWithContent<ApiErrorManagerEvent>()
    override val events: StateEventFlowWithContent<ApiErrorManagerEvent> =
        _events.asStateEventWithContentFlow()

    override fun emitEvent(event: ApiErrorManagerEvent) {
        _events.trigger(event)
    }
}