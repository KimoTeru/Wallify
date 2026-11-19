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
import org.jetbrains.compose.resources.getString
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.flow.MutableStateEventFlowWithContent
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.ResponseErrorType
import org.kimoteru.wallify.core.presentation.base.BaseContract.BaseUiState
import org.kimoteru.wallify.core.presentation.base.navigation.ScreenBackResult
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.NotifierManager
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.domain.model.NotifierModel
import org.kimoteru.wallify.core.presentation.errors.ApiError
import org.kimoteru.wallify.core.presentation.errors.ApiFailure
import org.kimoteru.wallify.core.presentation.errors.DefaultApiError
import org.kimoteru.wallify.core.presentation.errors.resolveApiFailure
import org.kimoteru.wallify.core.presentation.errors.toApiError
import org.kimoteru.wallify.core.presentation.extensions.collectAsEffect
import org.kimoteru.wallify.core.presentation.extensions.errorMessage
import org.kimoteru.wallify.core.presentation.extensions.showError
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_retry
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
    fun getBaseUiState(): BaseUiState = BaseUiState()

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
    override fun getBaseUiState(): BaseUiState {
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

    private val notifierManager by inject<NotifierManager>()

    /** The snackbar with a repeat this logic showed last; it closes with the logic ([onDestroy]). */
    private var retrySnackbar: NotifierModel? = null

    /** Lists whose failed refresh is already in the snackbar ([showListRefreshError]). */
    private val failedLists = mutableSetOf<Long>()

    val navigateBack: StateEventFlowWithContent<ScreenBackResult?>
        field = MutableStateEventFlowWithContent()

    val navigateEvent: StateEventFlowWithContent<NavigateEvent>
        field = MutableStateEventFlowWithContent()

    val componentEvents: StateEventFlowWithContent<ComponentEvent>
        field = MutableStateEventFlowWithContent()

    val baseUiState: StateFlow<BaseUiState>
        field = MutableStateFlow(BaseUiState())

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

    /**
     * Runs one action of the screen and handles its failure (`code/presentation.md` § Ошибка действия). Nothing is
     * repeated by itself. A status from [Error] shows its message and goes to [onTypedError]; 401 shows nothing, the
     * data layer has already ended the session; the exhausted request limit shows its text without a repeat; any
     * other failure shows the text by the type of the error with «Повторить», which runs [onRetry] or, without it,
     * the same [request] with the same callbacks (offline the root keeps its own snackbar instead). [onError] gets
     * every failure after it was handled, for the screen's own state.
     */
    protected suspend inline fun <T, reified Error> handleResponse(
        noinline request: suspend () -> Response<T>,
        noinline onSuccess: suspend (T) -> Unit = {},
        noinline onTypedError: suspend (Error) -> Unit = {},
        noinline onError: suspend (ResponseError) -> Unit = {},
        noinline onRetry: (suspend () -> Unit)? = null,
    ) where Error : Enum<Error>, Error : ApiError = handleApiResponse(
        request = request,
        toTypedError = { code -> code.toApiError<Error>() },
        onSuccess = onSuccess,
        onTypedError = onTypedError,
        onError = onError,
        onRetry = onRetry
    )

    /** [handleResponse] for an action without statuses of its own. */
    protected suspend fun <T> handleResponseDefault(
        request: suspend () -> Response<T>,
        onSuccess: suspend (T) -> Unit = {},
        onError: suspend (ResponseError) -> Unit = {},
        onRetry: (suspend () -> Unit)? = null,
    ) = handleResponse<T, DefaultApiError>(
        request = request,
        onSuccess = onSuccess,
        onError = onError,
        onRetry = onRetry
    )

    /**
     * Shows [error] in the root snackbar with «Повторить», which runs [onRetry] in the scope of this logic. The
     * exhausted request limit gets no button: a repeat right away fails the same way. Offline the root shows nothing
     * of it: its own snackbar about the internet is up.
     */
    protected suspend fun showErrorWithRetry(error: ResponseError, onRetry: suspend () -> Unit) {
        if (error.type == ResponseErrorType.RateLimited) {
            notifierManager.showError(error)
            return
        }

        retrySnackbar = notifierManager.showWithAction(
            message = error.errorMessage(),
            actionLabel = getString(Res.string.common_retry)
        ) {
            coroutineScope.launch { onRetry() }
        }
    }

    @PublishedApi
    internal suspend fun <T, Error : ApiError> handleApiResponse(
        request: suspend () -> Response<T>,
        toTypedError: (Int) -> Error?,
        onSuccess: suspend (T) -> Unit,
        onTypedError: suspend (Error) -> Unit,
        onError: suspend (ResponseError) -> Unit,
        onRetry: (suspend () -> Unit)?,
    ) {
        val error = when (val response = request()) {
            is Response.Success -> {
                onSuccess(response.data)
                return
            }
            is Response.Error -> response.errorData
        }

        when (val failure = resolveApiFailure(error, toTypedError(error.code))) {
            is ApiFailure.Typed -> {
                failure.error.message?.let { message -> notifierManager.showLong(getString(message)) }
                onTypedError(failure.error)
            }
            // The data layer ends the session on 401 itself, and the root shows the sign-in.
            is ApiFailure.SessionExpired -> Unit
            is ApiFailure.RateLimited -> notifierManager.showError(error)
            is ApiFailure.Retryable -> showErrorWithRetry(error) {
                if (onRetry != null) {
                    onRetry()
                } else {
                    handleApiResponse(request, toTypedError, onSuccess, onTypedError, onError, onRetry = null)
                }
            }
        }
        onError(error)
    }

    /**
     * Shows a failed refresh of the list [list] over shown items (`RefreshErrorEffect`) with «Повторить», which runs
     * [onRetry]; `null` [error] means the failure of the list is over. A failure is shown once per list until the list
     * reports it over: the screen recreated after a rotation reports the same failure again. [onRetry] does not hold
     * the list: the screen counts the press in its state, and the failed list on screen repeats itself.
     */
    protected fun showListRefreshError(list: Long, error: ResponseError?, onRetry: () -> Unit) {
        if (error == null) {
            failedLists.remove(list)
            return
        }
        if (!failedLists.add(list)) return

        coroutineScope.launch { showErrorWithRetry(error) { onRetry() } }
    }

    override fun onDestroy() {
        // A repeat belongs to this screen: its snackbar must not stay over the next one. The repeat itself could not
        // run anyway, it is launched in the scope cancelled below.
        retrySnackbar?.let(notifierManager::dismiss)
        coroutineScope.cancel()
        super.onDestroy()
    }
}