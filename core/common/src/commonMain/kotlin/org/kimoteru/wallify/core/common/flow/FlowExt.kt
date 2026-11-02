package org.kimoteru.wallify.core.common.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun <T, R> Flow<T>.mapDistinctBy(selector: (T) -> R): Flow<R> =
    map { selector(it) }.distinctUntilChanged()

fun <T, R> StateFlow<T>.mapDistinctBy(
    coroutineScope: CoroutineScope,
    selector: (T) -> R
): StateFlow<R> =
    map { selector(it) }
        .distinctUntilChanged()
        .stateIn(coroutineScope, SharingStarted.Eagerly, selector(value))

fun <T> Flow<T>.dropFirst(): Flow<T> = drop(1)

suspend fun <T> StateFlow<T>.awaitUntil(predicate: suspend (T) -> Boolean): T {
    val current = value
    if (predicate(current)) return current
    return filter { predicate(it) }.first()
}

suspend fun <T> Flow<T>.awaitUntil(predicate: suspend (T) -> Boolean): T {
    val current = first()
    if (predicate(current)) return current
    return filter { predicate(it) }.first()
}