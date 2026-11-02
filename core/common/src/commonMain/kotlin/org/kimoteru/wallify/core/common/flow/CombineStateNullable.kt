@file:Suppress("UNCHECKED_CAST")

package org.kimoteru.wallify.core.common.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun <T1, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>?,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?) -> R
): StateFlow<R> =
    (f1 ?: flowOf(null)).map { mapper(it) }
        .stateIn(scope, started, initialValue)

fun <T1, T2, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>?,
    f2: Flow<T2>?,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?) -> R
): StateFlow<R> = combine(
    f1 ?: flowOf(null),
    f2 ?: flowOf(null),
    mapper
).stateIn(scope, started, initialValue)

fun <T1, T2, T3, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>?,
    f2: Flow<T2>?,
    f3: Flow<T3>?,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?) -> R
): StateFlow<R> = combine(
    f1 ?: flowOf(null),
    f2 ?: flowOf(null),
    f3 ?: flowOf(null),
    mapper
).stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>?,
    f2: Flow<T2>?,
    f3: Flow<T3>?,
    f4: Flow<T4>?,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?) -> R
): StateFlow<R> = combine(
    f1 ?: flowOf(null),
    f2 ?: flowOf(null),
    f3 ?: flowOf(null),
    f4 ?: flowOf(null),
    mapper
).stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>?,
    f2: Flow<T2>?,
    f3: Flow<T3>?,
    f4: Flow<T4>?,
    f5: Flow<T5>?,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?) -> R
): StateFlow<R> = combine(
    f1 ?: flowOf(null),
    f2 ?: flowOf(null),
    f3 ?: flowOf(null),
    f4 ?: flowOf(null),
    f5 ?: flowOf(null),
    mapper
).stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, T6, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>?,
    f2: Flow<T2>?,
    f3: Flow<T3>?,
    f4: Flow<T4>?,
    f5: Flow<T5>?,
    f6: Flow<T6>?,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?
        )
    }.stateIn(scope, started, initialValue)
}

fun <T1, T2, T3, T4, T5, T6, T7, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>? = null,
    f2: Flow<T2>? = null,
    f3: Flow<T3>? = null,
    f4: Flow<T4>? = null,
    f5: Flow<T5>? = null,
    f6: Flow<T6>? = null,
    f7: Flow<T7>? = null,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?, T7?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null),
        f7 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?,
            values[6] as T7?
        )
    }.stateIn(scope, started, initialValue)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>? = null,
    f2: Flow<T2>? = null,
    f3: Flow<T3>? = null,
    f4: Flow<T4>? = null,
    f5: Flow<T5>? = null,
    f6: Flow<T6>? = null,
    f7: Flow<T7>? = null,
    f8: Flow<T8>? = null,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?, T7?, T8?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null),
        f7 ?: flowOf(null),
        f8 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?,
            values[6] as T7?,
            values[7] as T8?
        )
    }.stateIn(scope, started, initialValue)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>? = null,
    f2: Flow<T2>? = null,
    f3: Flow<T3>? = null,
    f4: Flow<T4>? = null,
    f5: Flow<T5>? = null,
    f6: Flow<T6>? = null,
    f7: Flow<T7>? = null,
    f8: Flow<T8>? = null,
    f9: Flow<T9>? = null,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?, T7?, T8?, T9?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null),
        f7 ?: flowOf(null),
        f8 ?: flowOf(null),
        f9 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?,
            values[6] as T7?,
            values[7] as T8?,
            values[8] as T9?
        )
    }.stateIn(scope, started, initialValue)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>? = null,
    f2: Flow<T2>? = null,
    f3: Flow<T3>? = null,
    f4: Flow<T4>? = null,
    f5: Flow<T5>? = null,
    f6: Flow<T6>? = null,
    f7: Flow<T7>? = null,
    f8: Flow<T8>? = null,
    f9: Flow<T9>? = null,
    f10: Flow<T10>? = null,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?, T7?, T8?, T9?, T10?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null),
        f7 ?: flowOf(null),
        f8 ?: flowOf(null),
        f9 ?: flowOf(null),
        f10 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?,
            values[6] as T7?,
            values[7] as T8?,
            values[8] as T9?,
            values[9] as T10?
        )
    }.stateIn(scope, started, initialValue)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>? = null,
    f2: Flow<T2>? = null,
    f3: Flow<T3>? = null,
    f4: Flow<T4>? = null,
    f5: Flow<T5>? = null,
    f6: Flow<T6>? = null,
    f7: Flow<T7>? = null,
    f8: Flow<T8>? = null,
    f9: Flow<T9>? = null,
    f10: Flow<T10>? = null,
    f11: Flow<T11>? = null,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?, T7?, T8?, T9?, T10?, T11?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null),
        f7 ?: flowOf(null),
        f8 ?: flowOf(null),
        f9 ?: flowOf(null),
        f10 ?: flowOf(null),
        f11 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?,
            values[6] as T7?,
            values[7] as T8?,
            values[8] as T9?,
            values[9] as T10?,
            values[10] as T11?
        )
    }.stateIn(scope, started, initialValue)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>? = null,
    f2: Flow<T2>? = null,
    f3: Flow<T3>? = null,
    f4: Flow<T4>? = null,
    f5: Flow<T5>? = null,
    f6: Flow<T6>? = null,
    f7: Flow<T7>? = null,
    f8: Flow<T8>? = null,
    f9: Flow<T9>? = null,
    f10: Flow<T10>? = null,
    f11: Flow<T11>? = null,
    f12: Flow<T12>? = null,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?, T7?, T8?, T9?, T10?, T11?, T12?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null),
        f7 ?: flowOf(null),
        f8 ?: flowOf(null),
        f9 ?: flowOf(null),
        f10 ?: flowOf(null),
        f11 ?: flowOf(null),
        f12 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?,
            values[6] as T7?,
            values[7] as T8?,
            values[8] as T9?,
            values[9] as T10?,
            values[10] as T11?,
            values[11] as T12?
        )
    }.stateIn(scope, started, initialValue)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> combineStateNullable(
    scope: CoroutineScope,
    f1: Flow<T1>? = null,
    f2: Flow<T2>? = null,
    f3: Flow<T3>? = null,
    f4: Flow<T4>? = null,
    f5: Flow<T5>? = null,
    f6: Flow<T6>? = null,
    f7: Flow<T7>? = null,
    f8: Flow<T8>? = null,
    f9: Flow<T9>? = null,
    f10: Flow<T10>? = null,
    f11: Flow<T11>? = null,
    f12: Flow<T12>? = null,
    f13: Flow<T13>? = null,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1?, T2?, T3?, T4?, T5?, T6?, T7?, T8?, T9?, T10?, T11?, T12?, T13?) -> R
): StateFlow<R> {
    val flows = listOf(
        f1 ?: flowOf(null),
        f2 ?: flowOf(null),
        f3 ?: flowOf(null),
        f4 ?: flowOf(null),
        f5 ?: flowOf(null),
        f6 ?: flowOf(null),
        f7 ?: flowOf(null),
        f8 ?: flowOf(null),
        f9 ?: flowOf(null),
        f10 ?: flowOf(null),
        f11 ?: flowOf(null),
        f12 ?: flowOf(null),
        f13 ?: flowOf(null)
    )

    return combine(flows) { values ->
        mapper(
            values[0] as T1?,
            values[1] as T2?,
            values[2] as T3?,
            values[3] as T4?,
            values[4] as T5?,
            values[5] as T6?,
            values[6] as T7?,
            values[7] as T8?,
            values[8] as T9?,
            values[9] as T10?,
            values[10] as T11?,
            values[11] as T12?,
            values[12] as T13?
        )
    }.stateIn(scope, started, initialValue)
}