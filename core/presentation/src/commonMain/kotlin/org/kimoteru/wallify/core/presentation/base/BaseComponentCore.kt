package org.kimoteru.wallify.core.presentation.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.kimoteru.wallify.core.presentation.base.navigation.ScreenBackResult
import org.kimoteru.wallify.core.presentation.extensions.collectAsEffect
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface BaseComponentCore<
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    ComponentEvent : BaseContract.ComponentEvent
> {
    @Composable
    fun getUiState(): UiState

    @Composable
    fun getBaseUiState(): BaseContract.BaseUiState = BaseContract.BaseUiState()

    @Composable
    fun collectComponentEvents(onEvent: (ComponentEvent) -> Unit) = Unit

    fun obtainUiEvent(event: UiEvent) = Unit

    fun obtainBaseEvent(baseEvent: BaseContract.UiEvent) = Unit
}

abstract class BaseDefaultComponentCore<
    Logic : BaseLogicCore<UiState, UiEvent, NavigationEvent, ComponentEvent>,
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    NavigationEvent : BaseNavigationEvent,
    ComponentEvent : BaseContract.ComponentEvent
>(
    componentContext: ComponentContext,
    private val navigateBack: (() -> Unit)? = null
) : ComponentContext by componentContext,
    BaseComponentCore<UiState, UiEvent, ComponentEvent>,
    KoinComponent {

    protected abstract val logic: Logic

    protected val dispatchers by inject<AppDispatchers>()
    protected val coroutineScope = coroutineScope(SupervisorJob() + dispatchers.main)

    val componentEvents: StateEventFlowWithContent<ComponentEvent>
        field = MutableStateEventFlowWithContent()

    init {
        coroutineScope.launch {
            logic.apply {
                doOnStart { onStart() }
                doOnResume { onResume() }
                doOnPause { onPause() }
                doOnStop { onStop() }
                doOnDestroy { onLifecycleDestroy() }
            }

            navigateBack?.let { callback ->
                launch {
                    logic.navigateBack.collectLatest {
                        callback()
                    }
                }
            }

            launch {
                logic.navigateEvent.collectLatest { event ->
                    obtainNavigateEvent(event)
                }
            }

            launch {
                logic.componentEvents.collectLatest { event ->
                    componentEvents.trigger(event)
                }
            }
        }
    }

    fun navigateBack(result: ScreenBackResult? = null) {
        navigateBack?.invoke()
    }

    /**
     * The logic of the screen, kept across configuration changes like a ViewModel. [factory] runs only when there is
     * no kept instance yet, so [BaseLogicCore.onCreate] runs there, once per logic, and not on every recreated
     * component.
     */
    protected inline fun <reified L : Logic> retainedLogic(factory: () -> L): L =
        retainedInstance { factory().apply { onCreate() } }

    @Composable
    override fun getUiState(): UiState {
        val uiState by logic.uiState.collectAsState()
        return uiState
    }

    @Composable
    override fun getBaseUiState(): BaseContract.BaseUiState {
        val baseUiState by logic.baseUiState.collectAsState()
        return baseUiState
    }

    protected open fun obtainNavigateEvent(event: NavigationEvent) = Unit

    override fun obtainUiEvent(event: UiEvent) = logic.obtainUiEvent(event)

    override fun obtainBaseEvent(baseEvent: BaseContract.UiEvent) = logic.obtainBaseEvent(baseEvent)

    @Composable
    override fun collectComponentEvents(onEvent: (ComponentEvent) -> Unit) {
        componentEvents.collectAsEffect { event ->
            onEvent(event)
        }
    }
}

abstract class BaseLogicCore<
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    NavigateEvent : BaseNavigationEvent,
    ComponentEvent : BaseContract.ComponentEvent
> : InstanceKeeper.Instance, KoinComponent {

    protected val dispatchers by inject<AppDispatchers>()
    protected val coroutineScope = CoroutineScope(SupervisorJob() + dispatchers.main)

    val navigateBack: StateEventFlowWithContent<ScreenBackResult?>
        field = MutableStateEventFlowWithContent()

    val navigateEvent: StateEventFlowWithContent<NavigateEvent>
        field = MutableStateEventFlowWithContent()

    val componentEvents: StateEventFlowWithContent<ComponentEvent>
        field = MutableStateEventFlowWithContent()

    val baseUiState: StateFlow<BaseContract.BaseUiState>
        field = MutableStateFlow(BaseContract.BaseUiState())

    abstract val uiState: StateFlow<UiState>

    protected open val defaultBackResult: ScreenBackResult? = null

    /**
     * Runs once, when the logic is created (`retainedLogic`), like the `init` of a ViewModel: the logic outlives a
     * recreated component. The other callbacks follow the lifecycle of the current component.
     */
    open fun onCreate() = Unit
    open fun onStart() = Unit
    open fun onResume() = Unit
    open fun onPause() = Unit
    open fun onStop() = Unit
    open fun onLifecycleDestroy() = Unit

    protected fun sendComponentEvent(event: ComponentEvent) {
        componentEvents.trigger(event)
    }

    open fun navigateBack(result: ScreenBackResult? = null) {
        navigateBack.trigger(result ?: defaultBackResult)
    }

    protected fun navigate(event: NavigateEvent) {
        navigateEvent.trigger(event)
    }

    open fun obtainBackNavigationResult(result: ScreenBackResult?) = Unit

    open fun obtainBaseEvent(baseEvent: BaseContract.UiEvent) {
        when (baseEvent) {
            is BaseContract.UiEvent.NavigateBack -> navigateBack(defaultBackResult)
        }
    }

    open fun obtainUiEvent(event: UiEvent) = Unit

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}