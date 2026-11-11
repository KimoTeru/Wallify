package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSnackbar
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract.UiState

interface NotifierManagerContract {
    @Immutable
    data class UiState(
        val notifierModel: NotifierModel? = null
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        /** The button of the snackbar was pressed. */
        data object PerformAction : UiEvent
    }
}

/** Snackbar of the root over every screen, always drawn as [GlassSnackbar]. */
@Composable
fun NotifierManagerUi(
    modifier: Modifier = Modifier,
    uiState: UiState,
    obtainUiEvent: (UiEvent) -> Unit
) {
    val hostState = remember { SnackbarHostState() }

    val appPadding = AppTheme.padding
    val appZIndex = AppTheme.zIndex

    LaunchedEffect(uiState.notifierModel) {
        val event = uiState.notifierModel
        hostState.currentSnackbarData?.dismiss()
        if (event == null || event.message.isBlank()) return@LaunchedEffect

        hostState.showSnackbar(
            message = event.message,
            actionLabel = event.actionLabel,
            duration = event.snackbarDuration
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(appZIndex.notifications)
    ) {
        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(appPadding.medium),
            hostState = hostState
        ) { data ->
            // The logic closes the snackbar and runs the action: the host only draws it.
            GlassSnackbar(
                message = data.visuals.message,
                actionLabel = data.visuals.actionLabel,
                onAction = { obtainUiEvent(UiEvent.PerformAction) }
            )
        }
    }
}