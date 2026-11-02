package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model

import androidx.compose.material3.SnackbarDuration

data class NotifierModel(
    val message: String,
    val snackbarDuration: SnackbarDuration
)