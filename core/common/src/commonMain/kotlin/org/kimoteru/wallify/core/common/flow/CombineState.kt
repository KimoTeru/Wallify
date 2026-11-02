package org.kimoteru.wallify.core.common.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun <T1, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1) -> R
): StateFlow<R> =
    f1
        .map(mapper)
        .stateIn(scope, started, initialValue)

fun <T1, T2, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2) -> R
): StateFlow<R> =
    combine(f1, f2) { t1, t2 -> mapper(t1, t2) }
        .stateIn(scope, started, initialValue)

fun <T1, T2, T3, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3) -> R
): StateFlow<R> =
    combine(f1, f2, f3) { t1, t2, t3 -> mapper(t1, t2, t3) }
        .stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3, T4) -> R
): StateFlow<R> =
    combine(f1, f2, f3, f4) { t1, t2, t3, t4 -> mapper(t1, t2, t3, t4) }
        .stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3, T4, T5) -> R
): StateFlow<R> =
    combine(f1, f2, f3, f4, f5) { t1, t2, t3, t4, t5 -> mapper(t1, t2, t3, t4, t5) }
        .stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, T6, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3, T4, T5, T6) -> R
): StateFlow<R> =
    combine(f1, f2, f3, f4, f5, f6) { t1, t2, t3, t4, t5, t6 -> mapper(t1, t2, t3, t4, t5, t6) }
        .stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, T6, T7, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3, T4, T5, T6, T7) -> R
): StateFlow<R> =
    combine(f1, f2, f3, f4, f5, f6, f7) { t1, t2, t3, t4, t5, t6, t7 ->
        mapper(t1, t2, t3, t4, t5, t6, t7)
    }.stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    f8: Flow<T8>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): StateFlow<R> =
    combine(f1, f2, f3, f4, f5, f6, f7, f8) { t1, t2, t3, t4, t5, t6, t7, t8 ->
        mapper(t1, t2, t3, t4, t5, t6, t7, t8)
    }.stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    f8: Flow<T8>,
    f9: Flow<T9>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): StateFlow<R> =
    combine(f1, f2, f3, f4, f5, f6, f7, f8, f9) { t1, t2, t3, t4, t5, t6, t7, t8, t9 ->
        mapper(t1, t2, t3, t4, t5, t6, t7, t8, t9)
    }.stateIn(scope, started, initialValue)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combineState(
    scope: CoroutineScope,
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    f8: Flow<T8>,
    f9: Flow<T9>,
    f10: Flow<T10>,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R
): StateFlow<R> =
    combine(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10) { t1, t2, t3, t4, t5, t6, t7, t8, t9, t10 ->
        mapper(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
    }.stateIn(scope, started, initialValue)