package org.kimoteru.wallify.core.designsystem.ui.modifier

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize

enum class SyncMode {
    WIDTH,
    HEIGHT,
    BOTH
}

enum class SyncedSizeSlotType2 {
    FIRST,
    SECOND
}

@Immutable
data class MeasuredSize(val value: DpSize? = null)

@Immutable
data class SyncedSizeGroup2(
    val first: MeasuredSize = MeasuredSize(),
    val second: MeasuredSize = MeasuredSize(),
    val synced: DpSize? = null,
    val mode: SyncMode = SyncMode.BOTH
) {
    fun updateFirst(size: DpSize): SyncedSizeGroup2 {
        val next = copy(first = first.copy(value = size))
        return next.copy(synced = maxSyncedSizeOf(next.first, next.second))
    }

    fun updateSecond(size: DpSize): SyncedSizeGroup2 {
        val next = copy(second = second.copy(value = size))
        return next.copy(synced = maxSyncedSizeOf(next.first, next.second))
    }

    fun update(slot: SyncedSizeSlotType2, size: DpSize): SyncedSizeGroup2 = when (slot) {
        SyncedSizeSlotType2.FIRST -> updateFirst(size)
        SyncedSizeSlotType2.SECOND -> updateSecond(size)
    }
}

fun maxSyncedSizeOf(vararg measured: MeasuredSize): DpSize? {
    val filtered = measured.mapNotNull { it.value }
    if (filtered.isEmpty()) return null
    val maxW = filtered.maxOf { it.width }
    val maxH = filtered.maxOf { it.height }
    return DpSize(maxW, maxH)
}

fun Modifier.trackMeasuredSize(
    measured: MeasuredSize,
    density: Density,
    onMeasured: (DpSize) -> Unit
): Modifier = applyIf(measured.value == null) {
    onSizeChanged { size ->
        with(density) { onMeasured(DpSize(size.width.toDp(), size.height.toDp())) }
    }
}

fun Modifier.applySyncedSize(
    synced: DpSize?,
    mode: SyncMode = SyncMode.BOTH
): Modifier = applyIfNotNull {
    synced?.let { s ->
        val target = when (mode) {
            SyncMode.WIDTH -> DpSize(s.width, Dp.Unspecified)
            SyncMode.HEIGHT -> DpSize(Dp.Unspecified, s.height)
            SyncMode.BOTH -> s
        }

        size(target)
    }
}

fun Modifier.syncedSize(
    measured: MeasuredSize,
    synced: DpSize?,
    density: Density,
    onMeasured: (DpSize) -> Unit,
    applyMode: SyncMode = SyncMode.BOTH
): Modifier = this
    .trackMeasuredSize(
        measured = measured,
        density = density,
        onMeasured = onMeasured
    )
    .applySyncedSize(synced, applyMode)

fun Modifier.syncedSize2(
    group: SyncedSizeGroup2,
    slot: SyncedSizeSlotType2,
    density: Density,
    onMeasured: (DpSize) -> Unit
): Modifier = syncedSize(
    measured = when (slot) {
        SyncedSizeSlotType2.FIRST -> group.first
        SyncedSizeSlotType2.SECOND -> group.second
    },
    synced = group.synced,
    density = density,
    onMeasured = onMeasured,
    applyMode = group.mode
)