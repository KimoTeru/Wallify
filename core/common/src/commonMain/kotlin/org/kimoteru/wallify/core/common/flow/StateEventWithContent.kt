package org.kimoteru.wallify.core.common.flow

sealed interface StateEventWithContent<out T>

data class StateEventWithContentTriggered<T>(val content: T) : StateEventWithContent<T> {
    override fun toString(): String = "triggered($content)"
}

data object StateEventWithContentConsumed : StateEventWithContent<Nothing> {
    override fun toString(): String = "consumed"
}