package org.kimoteru.wallify.core.presentation.extensions

import org.jetbrains.compose.resources.getString
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.designsystem.ui.network.toPresentation
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager

/**
 * Text of a failed action for the snackbar, by the type of the error and with its HTTP status code. For logic: the
 * snackbar is drawn by the root, so the logic resolves the text itself (`code/resources.md` § Строки).
 */
suspend fun ResponseError.errorMessage(): String {
    val type = type.toPresentation()

    return if (type.hasCode) getString(type.description, code.toString()) else getString(type.description)
}

/** Shows the failure of an action by its [error] in the root snackbar. */
suspend fun NotifierManager.showError(error: ResponseError) = showLong(error.errorMessage())