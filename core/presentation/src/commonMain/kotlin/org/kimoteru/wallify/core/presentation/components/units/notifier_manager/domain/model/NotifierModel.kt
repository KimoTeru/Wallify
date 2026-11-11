package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model

import androidx.compose.material3.SnackbarDuration

/**
 * One snackbar of the root. With [actionLabel] and [onAction] it has a button: pressing it closes the snackbar and
 * runs [onAction] (a repeat of the failed action, started in the scope of the screen that asked for it).
 */
data class NotifierModel(
    val message: String,
    val snackbarDuration: SnackbarDuration,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)