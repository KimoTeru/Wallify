package org.kimoteru.wallify.core.presentation.components.units.api_error_manager.data

import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManager
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.domain.ApiErrorManagerEvent
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.koin.core.annotation.Singleton

@Singleton
class ApiErrorManagerImpl : ApiErrorManager {
    override val events: StateEventFlowWithContent<ApiErrorManagerEvent>
        field = MutableStateEventFlowWithContent<ApiErrorManagerEvent>()

    override fun emitEvent(event: ApiErrorManagerEvent) {
        events.trigger(event)
    }
}