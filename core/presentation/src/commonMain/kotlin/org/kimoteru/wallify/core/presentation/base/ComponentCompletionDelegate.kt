package org.kimoteru.wallify.core.presentation.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ComponentCompletionDelegate {
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    fun complete() {
        _isCompleted.update { true }
    }

    fun reset() {
        _isCompleted.update { false }
    }
}