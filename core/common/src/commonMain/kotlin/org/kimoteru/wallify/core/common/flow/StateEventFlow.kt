package org.kimoteru.wallify.core.common.flow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update

interface StateEventFlow {
    suspend fun collectLatest(block: suspend () -> Unit)
    suspend fun collect(block: suspend () -> Unit)

    val child: StateFlow<StateEvent>
}

class MutableStateEventFlow(defaultValue: StateEvent = stateEventConsumed) : StateEventFlow {
    private val stateEventFlow = MutableStateFlow(defaultValue)

    override val child = stateEventFlow.asStateFlow()

    override suspend fun collectLatest(block: suspend () -> Unit) {
        stateEventFlow.collect { stateEvent ->
            when (stateEvent) {
                is StateEvent.Consumed -> Unit
                is StateEvent.Triggered -> {
                    block()
                    stateEventFlow.emit(stateEventConsumed)
                }
            }
        }
    }

    override suspend fun collect(block: suspend () -> Unit) {
        stateEventFlow.collect { stateEvent ->
            when (stateEvent) {
                is StateEvent.Consumed -> Unit
                is StateEvent.Triggered -> block()
            }
        }
    }

    fun consumed() {
        stateEventFlow.update { stateEventConsumed }
    }

    fun trigger() {
        stateEventFlow.update { stateEventTriggered }
    }
}

fun MutableStateEventFlow.asStateEventFlow(): StateEventFlow = this

interface StateEventFlowWithContent<T> {
    suspend fun collectLatest(block: suspend (T) -> Unit)
    suspend fun collect(block: suspend (T) -> Unit)
}

class MutableStateEventFlowWithContent<T>(
    defaultValue: StateEventWithContent<T> = StateEventWithContentConsumed
) : StateEventFlowWithContent<T> {
    private val stateEventFlow = MutableStateFlow(defaultValue)

    override suspend fun collectLatest(block: suspend (T) -> Unit) {
        stateEventFlow.collect { stateEvent ->
            when (stateEvent) {
                is StateEventWithContentTriggered<T> -> {
                    stateEventFlow.emit(StateEventWithContentConsumed)
                    block(stateEvent.content)
                }
                is StateEventWithContentConsumed -> return@collect
            }
        }
    }

    override suspend fun collect(block: suspend (T) -> Unit) {
        stateEventFlow.collect { stateEvent ->
            when (stateEvent) {
                is StateEventWithContentTriggered<T> -> block(stateEvent.content)
                is StateEventWithContentConsumed -> return@collect
            }
        }
    }

    fun trigger(value: T) {
        stateEventFlow.update { StateEventWithContentTriggered(value) }
    }
}

fun <T> MutableStateEventFlowWithContent<T>.asStateEventWithContentFlow(): StateEventFlowWithContent<T> = this