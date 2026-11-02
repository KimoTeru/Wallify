package org.kimoteru.wallify.core.presentation.extensions

import androidx.compose.material3.SnackbarDuration

fun SnackbarDuration.toMillis(): Long =
    when (this) {
        SnackbarDuration.Indefinite -> Long.MAX_VALUE
        SnackbarDuration.Long -> 10_000L
        SnackbarDuration.Short -> 4_000L
    }