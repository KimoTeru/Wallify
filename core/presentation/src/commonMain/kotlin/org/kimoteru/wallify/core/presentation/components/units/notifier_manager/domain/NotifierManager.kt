package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain

import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel

interface NotifierManager {
    val snackbarRequests: StateEventFlowWithContent<NotifierModel>

    fun showShort(message: String)
    fun showLong(message: String)
}