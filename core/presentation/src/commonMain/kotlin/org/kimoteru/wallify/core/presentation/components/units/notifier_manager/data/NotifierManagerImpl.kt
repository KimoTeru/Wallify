package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.data

import androidx.compose.material3.SnackbarDuration
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.asStateEventWithContentFlow
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel
import org.koin.core.annotation.Singleton

@Singleton
class NotifierManagerImpl : NotifierManager {

    private val _snackbarRequests = MutableStateEventFlowWithContent<NotifierModel>()
    override val snackbarRequests: StateEventFlowWithContent<NotifierModel> =
        _snackbarRequests.asStateEventWithContentFlow()

    override fun showShort(message: String) = emit(message, SnackbarDuration.Short)

    override fun showLong(message: String) = emit(message, SnackbarDuration.Long)

    private fun emit(message: String, duration: SnackbarDuration) {
        if (message.isBlank()) return
        _snackbarRequests.trigger(NotifierModel(message = message, snackbarDuration = duration))
    }
}