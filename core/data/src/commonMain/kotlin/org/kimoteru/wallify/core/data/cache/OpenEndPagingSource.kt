package org.kimoteru.wallify.core.data.cache

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * Wraps the Room source of a list that may have more pages on the backend ([isEnded] is false). Room answers
 * `nextKey = null` as soon as it has loaded the last saved row, and Paging then calls the `RemoteMediator` at once,
 * without any scroll: a refresh would cost two requests. Here a non-empty page that reached the saved end points to
 * the next offset instead, so Paging loads it (an empty page, `nextKey = null`) and calls the mediator only when the
 * user scrolls within `prefetchDistance` of the end. An empty page keeps `null`, or the same key would be loaded again.
 */
internal class OpenEndPagingSource<E : Any>(
    private val source: PagingSource<Int, E>,
    private val isEnded: suspend () -> Boolean,
) : PagingSource<Int, E>() {

    override val jumpingSupported: Boolean = source.jumpingSupported

    override val keyReuseSupported: Boolean = source.keyReuseSupported

    init {
        source.registerInvalidatedCallback(::invalidate)
        registerInvalidatedCallback(source::invalidate)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, E> {
        val result = source.load(params)
        if (result !is LoadResult.Page || result.nextKey != null || result.data.isEmpty() || isEnded()) return result

        val offset = if (result.itemsBefore == LoadResult.Page.COUNT_UNDEFINED) params.key ?: 0 else result.itemsBefore

        return result.copy(nextKey = offset + result.data.size)
    }

    override fun getRefreshKey(state: PagingState<Int, E>): Int? = source.getRefreshKey(state)
}