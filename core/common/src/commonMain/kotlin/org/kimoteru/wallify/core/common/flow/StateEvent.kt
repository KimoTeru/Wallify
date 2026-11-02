package org.kimoteru.wallify.core.common.flow

sealed interface StateEvent {
    data object Triggered : StateEvent {
        override fun toString(): String = "triggered"
    }

    data object Consumed : StateEvent {
        override fun toString(): String = "consumed"
    }
}

val stateEventTriggered = StateEvent.Triggered
val stateEventConsumed = StateEvent.Consumed