package org.kimoteru.wallify.core.presentation.components.default_components.default_bottom_sheet.presentation.ui

import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kimoteru.wallify.core.presentation.base.BaseComponentLogic
import org.kimoteru.wallify.core.presentation.components.default_components.default_bottom_sheet.presentation.ui.DefaultBottomSheetContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.default_components.default_bottom_sheet.presentation.ui.DefaultBottomSheetContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

fun KoinComponent.getDefaultBottomSheetLogic(): DefaultBottomSheetLogic {
    return get { parametersOf() }
}

@Factory
class DefaultBottomSheetLogic : BaseComponentLogic<UiState, UiEvent, Nothing>() {

    private val _uiState = MutableStateFlow(UiState())
    override val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.Show -> open()
            is UiEvent.Close -> close()
            is UiEvent.UpdateOffsetY -> updateOffsetY(event.y)
            is UiEvent.UpdateSheetHeight -> updateSheetHeight(event.height)
            is UiEvent.UpdateIsRefreshAnimatedOffset ->
                updateIsRefreshAnimatedOffset(event.isRefresh)
        }
    }

    fun open() {
        _uiState.update {
            it.copy(isVisible = true, offsetY = Dp.Hairline, primaryButtonEnabled = true)
        }
    }

    fun close() {
        _uiState.update {
            it.copy(
                isVisible = false,
                isRefreshAnimatedOffset = false
            )
        }
    }

    fun updatePrimaryButtonEnabled(
        enabled: Boolean
    ) {
        _uiState.update {
            it.copy(primaryButtonEnabled = enabled)
        }
    }

    private fun updateOffsetY(y: Dp) {
        _uiState.update {
            it.copy(offsetY = y)
        }
    }

    private fun updateSheetHeight(height: Dp) {
        _uiState.update {
            it.copy(sheetHeight = height)
        }
    }

    private fun updateIsRefreshAnimatedOffset(isRefreshing: Boolean) {
        _uiState.update {
            it.copy(isRefreshAnimatedOffset = isRefreshing)
        }
    }
}