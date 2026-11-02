package org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kimoteru.wallify.core.presentation.base.BaseLogic
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerContract.UiState
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

fun KoinComponent.getApiErrorManagerLogic(): ApiErrorManagerLogic {
    return get { parametersOf() }
}

@Factory
class ApiErrorManagerLogic : BaseLogic<UiState, UiEvent, Nothing>() {

    private val _uiState = MutableStateFlow(UiState())
    override val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    override fun obtainUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.HideDialog -> hideDialog()
        }
    }

    fun showDialog() = updateIsVisibleDialog(true)
    fun hideDialog() = updateIsVisibleDialog(false)

    private fun updateIsVisibleDialog(isVisible: Boolean) {
        _uiState.update { it.copy(isVisibleDialog = isVisible) }
    }
}