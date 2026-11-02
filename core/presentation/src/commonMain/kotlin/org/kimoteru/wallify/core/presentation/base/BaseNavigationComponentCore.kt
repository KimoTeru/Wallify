package org.kimoteru.wallify.core.presentation.base

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface BaseNavigationComponentCore<
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    Child : BaseNavigationComponentCore.BaseNavigationChild,
    ComponentEvent : BaseContract.ComponentEvent
> : BackHandlerOwner, BaseComponentCore<UiState, UiEvent, ComponentEvent> {
    val stack: Value<ChildStack<*, Child>>

    fun onBackClicked()
    fun onBackClicked(toIndex: Int)

    interface BaseNavigationChild
}

abstract class BaseNavigationDefaultComponentCore<
    Child : BaseNavigationComponentCore.BaseNavigationChild,
    Config : BaseNavigationDefaultComponentCore.BaseNavigationConfig,
    Logic : BaseNavigationLogicCore<UiState, UiEvent, NavigateEvent, ComponentEvent>,
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    NavigateEvent : BaseNavigationEvent,
    ComponentEvent : BaseContract.ComponentEvent
>(
    componentContext: ComponentContext,
    private val finishApp: (() -> Unit)? = null,
    private val navigateBackCallback: (() -> Unit)? = null
) : BaseDefaultComponentCore<Logic, UiState, UiEvent, NavigateEvent, ComponentEvent>(
    componentContext = componentContext,
    navigateBack = navigateBackCallback
),
    BaseNavigationComponentCore<UiState, UiEvent, Child, ComponentEvent> {

    protected val navigation = StackNavigation<Config>()
    protected abstract override val logic: Logic

    protected abstract fun child(
        config: Config,
        componentContext: ComponentContext
    ): Child

    protected fun navigate(configs: List<Config>, isNeedAdd: Boolean = false) {
        if (configs.isEmpty()) return
        navigation.navigate {
            if (isNeedAdd) it + configs else configs
        }
    }

    protected fun navigate(config: Config) {
        val configurations = stack.items.map { it.configuration }
        val existingIndex = configurations.indexOfFirst { it == config }
        if (existingIndex == -1) {
            navigation.navigate { it + config }
        } else {
            navigation.popTo(existingIndex)
        }
    }

    protected fun navigateClearBackStack(config: Config) {
        navigation.navigate { listOf(config) }
    }

    override fun onBackClicked() {
        val backStackIsEmpty = stack.value.backStack.isEmpty()
        if (backStackIsEmpty) {
            if (navigateBackCallback != null) {
                navigateBack(result = null)
                return
            }
            finishApp?.invoke()
            return
        }
        navigation.pop()
    }

    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
    }

    interface BaseNavigationConfig
}

abstract class BaseNavigationLogicCore<
    UiState : BaseContract.UiState,
    UiEvent : BaseContract.UiEvent,
    NavigationEvent : BaseNavigationEvent,
    ComponentEvent : BaseContract.ComponentEvent
> : BaseLogicCore<UiState, UiEvent, NavigationEvent, ComponentEvent>() {

    @Suppress("UNCHECKED_CAST")
    override val uiState: StateFlow<UiState> =
        MutableStateFlow(object : BaseContract.UiState {}) as StateFlow<UiState>
}