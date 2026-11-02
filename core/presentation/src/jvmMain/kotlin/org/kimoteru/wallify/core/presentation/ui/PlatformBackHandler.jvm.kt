package org.kimoteru.wallify.core.presentation.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) = Unit