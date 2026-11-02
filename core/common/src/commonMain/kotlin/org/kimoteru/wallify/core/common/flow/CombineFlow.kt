package org.kimoteru.wallify.core.common.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Suppress("UNCHECKED_CAST")
private fun <T> Array<*>.getAs(index: Int): T = this[index] as T

fun <
    T1, T2, T3, T4, T5, T6, R
    > combine(
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, R
    > combine(
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, R
    > combine(
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    f8: Flow<T8>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, T9, R
    > combine(
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    f7: Flow<T7>,
    f8: Flow<T8>,
    f9: Flow<T9>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8, f9)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7),
        values.getAs(8)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R
    > combine(
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
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7),
        values.getAs(8),
        values.getAs(9)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R
    > combine(
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
    f11: Flow<T11>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7),
        values.getAs(8),
        values.getAs(9),
        values.getAs(10)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R
    > combine(
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
    f11: Flow<T11>,
    f12: Flow<T12>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7),
        values.getAs(8),
        values.getAs(9),
        values.getAs(10),
        values.getAs(11)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R
    > combine(
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
    f11: Flow<T11>,
    f12: Flow<T12>,
    f13: Flow<T13>,
    transform: suspend (
        T1, T2, T3, T4, T5, T6, T7,
        T8, T9, T10, T11, T12, T13
    ) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7),
        values.getAs(8),
        values.getAs(9),
        values.getAs(10),
        values.getAs(11),
        values.getAs(12)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R
    > combine(
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
    f11: Flow<T11>,
    f12: Flow<T12>,
    f13: Flow<T13>,
    f14: Flow<T14>,
    transform: suspend (
        T1, T2, T3, T4, T5, T6, T7,
        T8, T9, T10, T11, T12, T13, T14
    ) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7),
        values.getAs(8),
        values.getAs(9),
        values.getAs(10),
        values.getAs(11),
        values.getAs(12),
        values.getAs(13)
    )
}

fun <
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R
    > combine(
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
    f11: Flow<T11>,
    f12: Flow<T12>,
    f13: Flow<T13>,
    f14: Flow<T14>,
    f15: Flow<T15>,
    transform: suspend (
        T1, T2, T3, T4, T5, T6, T7,
        T8, T9, T10, T11, T12, T13, T14, T15
    ) -> R
): Flow<R> = combine(
    *arrayOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15)
) { values ->
    transform(
        values.getAs(0),
        values.getAs(1),
        values.getAs(2),
        values.getAs(3),
        values.getAs(4),
        values.getAs(5),
        values.getAs(6),
        values.getAs(7),
        values.getAs(8),
        values.getAs(9),
        values.getAs(10),
        values.getAs(11),
        values.getAs(12),
        values.getAs(13),
        values.getAs(14)
    )
}