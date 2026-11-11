package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.data

import androidx.compose.material3.SnackbarDuration
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel
import org.koin.core.annotation.Singleton

@Singleton
class NotifierManagerImpl : NotifierManager {

    override val snackbarRequests: StateEventFlowWithContent<NotifierModel>
        field = MutableStateEventFlowWithContent<NotifierModel>()

    override val dismissRequests: StateEventFlowWithContent<NotifierModel>
        field = MutableStateEventFlowWithContent<NotifierModel>()

    override fun showShort(message: String) {
        emit(NotifierModel(message = message, snackbarDuration = SnackbarDuration.Short))
    }

    override fun showLong(message: String) {
        emit(NotifierModel(message = message, snackbarDuration = SnackbarDuration.Long))
    }

    override fun showWithAction(message: String, actionLabel: String, onAction: () -> Unit): NotifierModel? {
        val model = NotifierModel(
            message = message,
            snackbarDuration = SnackbarDuration.Long,
            actionLabel = actionLabel,
            onAction = onAction
        )

        return if (emit(model)) model else null
    }

    override fun dismiss(model: NotifierModel) = dismissRequests.trigger(model)

    private fun emit(model: NotifierModel): Boolean {
        if (model.message.isBlank()) return false
        snackbarRequests.trigger(model)
        return true
    }
}