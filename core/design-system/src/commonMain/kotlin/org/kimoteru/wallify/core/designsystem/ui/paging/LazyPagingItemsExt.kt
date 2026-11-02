package org.kimoteru.wallify.core.designsystem.ui.paging

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/** Stable key for the item at [index]: the item's own id when it is loaded, otherwise the index. */
inline fun <T : Any> LazyPagingItems<T>.keyAt(index: Int, crossinline id: (T) -> Any): Any =
    peek(index)?.let(id) ?: index

fun LazyPagingItems<*>.clampIndex(index: Int): Int = index.coerceIn(0, itemCount)

/** First page is loading (initial load or pull to refresh). */
val LazyPagingItems<*>.isRefreshing: Boolean
    get() = loadState.refresh is LoadState.Loading

/** The next page is loading, show a footer loader. */
val LazyPagingItems<*>.isAppending: Boolean
    get() = loadState.append is LoadState.Loading

/** Failure of the first page load, or null. */
val LazyPagingItems<*>.refreshError: Throwable?
    get() = (loadState.refresh as? LoadState.Error)?.error

/** Failure of the next page load, or null. Call `retry()` to try again. */
val LazyPagingItems<*>.appendError: Throwable?
    get() = (loadState.append as? LoadState.Error)?.error

/** Loading finished successfully and there is nothing to show. */
val LazyPagingItems<*>.isEmpty: Boolean
    get() = itemCount == 0 &&
        loadState.refresh is LoadState.NotLoading &&
        loadState.append.endOfPaginationReached