package org.kimoteru.wallify.core.presentation.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ComponentCompletionDelegate {
    val isCompleted: StateFlow<Boolean>
        field = MutableStateFlow(false)

    fun complete() {
        isCompleted.update { true }
    }

    fun reset() {
        isCompleted.update { false }
    }
}