package org.kimoteru.wallify.core.presentation.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import org.kimoteru.wallify.core.common.flow.StateEventFlowWithContent

@Composable
fun <T> StateEventFlowWithContent<T>.collectAsEffect(onEach: (T) -> Unit) {
    LaunchedEffect(this) {
        collectLatest { value ->
            onEach(value)
        }
    }
}