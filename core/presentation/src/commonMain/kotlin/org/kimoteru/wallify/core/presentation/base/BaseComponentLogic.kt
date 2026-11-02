package org.kimoteru.wallify.core.presentation.base

import kotlin.reflect.KProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlow
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.asStateEventFlow
import org.kimoteru.wallify.core.common.flow.asStateEventWithContentFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseComponentLogic<
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    NavigateEvent : BaseNavigationEvent
> : KoinComponent {

    protected val dispatchers by inject<AppDispatchers>()

    protected val _navigateBack = MutableStateEventFlow()
    val navigateBack = _navigateBack.asStateEventFlow()

    protected val _navigateEvent = MutableStateEventFlowWithContent<NavigateEvent>()
    val navigateEvent = _navigateEvent.asStateEventWithContentFlow()

    abstract val uiState: StateFlow<UiState>

    protected fun navigate(event: NavigateEvent) {
        _navigateEvent.trigger(event)
    }

    fun obtainBaseEvent(baseEvent: BaseContract.UiEvent) {
        when (baseEvent) {
            is BaseContract.UiEvent.NavigateBack -> _navigateBack.trigger()
        }
    }

    open fun obtainUiEvent(event: UiEvent) = Unit
}

class LogicComponentHolder<T : BaseComponentLogic<UiState, *, *>, UiState : BaseContract.UiState>(
    private val scope: CoroutineScope
) {
    private val _logic = MutableStateFlow<T?>(null)
    val logic: StateFlow<T?> = _logic.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState?> = _logic
        .flatMapLatest { it?.uiState ?: flowOf(null) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun set(logic: T?) {
        _logic.value = logic
    }

    fun get(): T? = _logic.value
}

class LogicComponentDelegate<T : BaseComponentLogic<UiState, *, *>, UiState : BaseContract.UiState>(
    private val holder: LogicComponentHolder<T, UiState>
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = holder.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = holder.set(value)
}