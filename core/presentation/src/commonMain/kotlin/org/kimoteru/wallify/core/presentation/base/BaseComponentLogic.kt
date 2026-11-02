package org.kimoteru.wallify.core.presentation.base

import kotlin.reflect.KProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlow
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.StateEventFlow
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseComponentLogic<
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    NavigateEvent : BaseNavigationEvent
> : KoinComponent {

    protected val dispatchers by inject<AppDispatchers>()

    val navigateBack: StateEventFlow
        field = MutableStateEventFlow()

    val navigateEvent: StateEventFlowWithContent<NavigateEvent>
        field = MutableStateEventFlowWithContent()

    abstract val uiState: StateFlow<UiState>

    protected fun navigate(event: NavigateEvent) {
        navigateEvent.trigger(event)
    }

    fun obtainBaseEvent(baseEvent: BaseContract.UiEvent) {
        when (baseEvent) {
            is BaseContract.UiEvent.NavigateBack -> navigateBack.trigger()
        }
    }

    open fun obtainUiEvent(event: UiEvent) = Unit
}

class LogicComponentHolder<T : BaseComponentLogic<UiState, *, *>, UiState : BaseContract.UiState>(
    private val scope: CoroutineScope
) {
    val logic: StateFlow<T?>
        field = MutableStateFlow<T?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState?> = logic
        .flatMapLatest { it?.uiState ?: flowOf(null) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun set(logic: T?) {
        this.logic.value = logic
    }

    fun get(): T? = logic.value
}

class LogicComponentDelegate<T : BaseComponentLogic<UiState, *, *>, UiState : BaseContract.UiState>(
    private val holder: LogicComponentHolder<T, UiState>
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = holder.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = holder.set(value)
}