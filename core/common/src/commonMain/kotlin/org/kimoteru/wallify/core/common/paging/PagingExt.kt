package org.kimoteru.wallify.core.common.paging

import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.filter

/** Drops items whose [keySelector] value was already seen, which protects lists from duplicate keys. */
inline fun <T : Any, K> PagingData<T>.distinctPagingBy(crossinline keySelector: (T) -> K): PagingData<T> {
    val keys = mutableSetOf<K>()
    return filter { item -> keys.add(keySelector(item)) }
}

/** Page-number refresh key: the page closest to the user's position, so a refresh keeps the scroll place. */
fun <T : Any> PagingState<Int, T>.getRefreshKeyOrNull(): Int? {
    val anchorPosition = anchorPosition ?: return null
    val closestPage = closestPageToPosition(anchorPosition)
    return closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
}