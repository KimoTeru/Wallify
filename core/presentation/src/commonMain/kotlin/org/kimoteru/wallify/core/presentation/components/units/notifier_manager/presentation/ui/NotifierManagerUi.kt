package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import org.kimoteru.wallify.core.common.extensions.half
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.card.DefaultRowCard
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract.UiState

interface NotifierManagerContract {
    @Immutable
    data class UiState(
        val notifierModel: NotifierModel? = null
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent
}

@Composable
fun NotifierManagerUi(
    modifier: Modifier = Modifier,
    uiState: UiState,
    obtainUiEvent: (UiEvent) -> Unit
) {
    val hostState = remember { SnackbarHostState() }

    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography
    val appZIndex = AppTheme.zIndex

    LaunchedEffect(uiState.notifierModel?.message) {
        val event = uiState.notifierModel ?: return@LaunchedEffect
        if (event.message.isBlank()) return@LaunchedEffect

        hostState.currentSnackbarData?.dismiss()
        hostState.showSnackbar(message = event.message, duration = event.snackbarDuration)
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
            DefaultRowCard(
                backgroundColor = appColor.basic.card.reversed,
                corners = appCorner.mediumSpecial,
                paddingValues = PaddingValues(
                    horizontal = appPadding.medium,
                    vertical = appPadding.mediumSpecial + appPadding.small.half
                )
            ) {
                Text(
                    text = data.visuals.message,
                    style = appTypography.h6.medium,
                    color = appColor.text.onReversed
                )
            }
        }
    }
}