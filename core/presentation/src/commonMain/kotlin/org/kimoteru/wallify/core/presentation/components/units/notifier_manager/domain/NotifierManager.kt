package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain

import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel

/** Snackbars of every screen; the root draws them, a screen only asks. */
interface NotifierManager {
    val snackbarRequests: StateEventFlowWithContent<NotifierModel>

    /** Snackbars that must close before their time, for example because the screen that asked for them closed. */
    val dismissRequests: StateEventFlowWithContent<NotifierModel>

    fun showShort(message: String)
    fun showLong(message: String)

    /**
     * Shows [message] with a button [actionLabel]; pressing it closes the snackbar and calls [onAction]. Returns the
     * shown snackbar for [dismiss], or `null` when [message] is blank and nothing is shown.
     */
    fun showWithAction(message: String, actionLabel: String, onAction: () -> Unit): NotifierModel?

    /** Closes [model] if it is still on screen; another snackbar shown after it stays. */
    fun dismiss(model: NotifierModel)
}