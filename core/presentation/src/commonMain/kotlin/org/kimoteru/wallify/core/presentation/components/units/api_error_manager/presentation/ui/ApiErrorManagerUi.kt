package org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withColor
import org.kimoteru.wallify.core.designsystem.ui.dialog.DefaultDialog
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerContract.UiState

interface ApiErrorManagerContract {
    @Immutable
    data class UiState(
        val isVisibleDialog: Boolean = false
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object HideDialog : UiEvent
    }
}

@Composable
fun ApiErrorManagerUi(
    modifier: Modifier = Modifier,
    uiState: UiState,
    obtainUiEvent: (UiEvent) -> Unit
) {
    val appColor = AppTheme.color
    val appTypography = AppTheme.typography

    if (uiState.isVisibleDialog) {
        DefaultDialog(
            modifier = modifier,
            onDismiss = {
                obtainUiEvent(UiEvent.HideDialog)
            },
            title = "Something went wrong",
            titleStyle = appTypography.h4.bold.withColor(appColor.basic.error),
            description = "Please try again later.",
            primaryButtonText = "OK",
            primaryButtonColor = appColor.basic.error,
            onPrimaryClick = {
                obtainUiEvent(UiEvent.HideDialog)
            }
        )
    }
}