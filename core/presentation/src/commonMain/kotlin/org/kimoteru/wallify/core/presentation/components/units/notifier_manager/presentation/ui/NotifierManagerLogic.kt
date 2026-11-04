package org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.presentation.base.BaseComponentLogic
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract.UiEvent
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract.UiState
import org.kimoteru.wallify.core.presentation.extensions.toMillis
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

fun KoinComponent.getNotifierManagerLogic(coroutineScope: CoroutineScope): NotifierManagerLogic {
    return get { parametersOf(coroutineScope) }
}

@Singleton
class NotifierManagerLogic(
    @InjectedParam private val coroutineScope: CoroutineScope
) : BaseComponentLogic<UiState, UiEvent, Nothing>() {

    override val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    init {
        coroutineScope.launch {
            uiState.mapDistinctBy { it.notifierModel }.collectLatest {
                it ?: return@collectLatest
                delay(it.snackbarDuration.toMillis())
                updateNotifierModel(null)
            }
        }
    }

    fun show(notifierModel: NotifierModel) = updateNotifierModel(notifierModel)

    fun dismiss() = updateNotifierModel(null)

    private fun updateNotifierModel(notifierModel: NotifierModel?) {
        uiState.update { it.copy(notifierModel = notifierModel) }
    }
}