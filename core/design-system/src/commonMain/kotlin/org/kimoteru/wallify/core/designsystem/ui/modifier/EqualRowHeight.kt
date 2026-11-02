package org.kimoteru.wallify.core.designsystem.ui.modifier

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.extensions.maxOf

@Stable
class EqualRowHeightsState(private val columns: Int) {

    private val itemHeights = mutableStateMapOf<Int, Dp>()
    private val itemKeys = mutableMapOf<Int, Any?>()

    fun getRowMaxHeight(index: Int): Dp? {
        val rowStart = (index / columns) * columns
        return (1 until columns).fold(itemHeights[rowStart]) { acc, offset ->
            maxOf(acc, itemHeights[rowStart + offset])
        }
    }

    fun getTrackedHeight(index: Int): Dp? = itemHeights[index]

    fun setHeight(index: Int, height: Dp) {
        itemHeights[index] = height
    }

    fun clearHeight(index: Int) {
        itemHeights.remove(index)
    }

    fun invalidateIfKeyChanged(index: Int, itemKey: Any?) {
        if (itemKeys[index] == itemKey) return

        itemKeys[index] = itemKey
        clearHeight(index)
    }
}

@Composable
fun rememberEqualRowHeights(columns: Int = 2): EqualRowHeightsState {
    return remember { EqualRowHeightsState(columns) }
}

fun Modifier.equalRowHeight(
    index: Int,
    state: EqualRowHeightsState,
    density: Density,
    itemKey: Any? = index
): Modifier = composed {
    LaunchedEffect(index, itemKey) {
        state.invalidateIfKeyChanged(index, itemKey)
    }

    this
        .applyIfNotNull { state.getRowMaxHeight(index)?.let { heightIn(min = it) } }
        .applyIfNull(state.getTrackedHeight(index)) {
            onSizeChanged { size ->
                state.setHeight(index, with(density) { size.height.toDp() })
            }
        }
}