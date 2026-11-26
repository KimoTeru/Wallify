package org.kimoteru.wallify.core.data.cache

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.paging.DEFAULT_PAGE_SIZE
import org.kimoteru.wallify.core.common.response.Response
import kotlin.time.Duration

/**
 * Paged list that the screen reads from Room: [pagingSource] reads the saved rows ([E]) in the order of the list
 * [listKey], [ListRemoteMediator] fills them from [fetch], and [toDomain] gives the rows to the layers above.
 * Collect it in the presentation layer with `cachedIn(scope)`. The mediator loads, decodes and saves the pages on
 * `dispatchers.io` of the repository, not on the main thread that collects the list.
 *
 * Paging calls the mediator for the next page whenever a load of the source returns `nextKey = null`
 * (`PageFetcherSnapshot`, not `prefetchDistance`), and Room does so right after the first page is saved, so a refresh
 * would cost two requests. [OpenEndPagingSource] hides that end until the list is saved to its last page, and
 * `prefetchDistance` is a third of a page: the next page is asked for only when the user scrolls near the end.
 *
 * Placeholders are on: Room invalidates the source on any write to its tables, and the reloaded source starts at the
 * refresh key `anchorPosition - initialLoadSize / 2`, which is an offset in the table. Without placeholders the
 * anchor is counted from the first loaded item, not from the table, so every second reload would start at the wrong
 * offset and the list would jump. With them the indexes of the list are the offsets, and the scroll position stays.
 */
@OptIn(ExperimentalPagingApi::class)
internal fun <E : Any, T : Any> CacheLoader.pager(
    listKey: String,
    ttl: Duration,
    dispatchers: AppDispatchers,
    pagingSource: () -> PagingSource<Int, E>,
    toDomain: (E) -> T,
    itemId: (T) -> String,
    fetch: suspend (page: Int, size: Int) -> Response<List<T>>,
    saveItems: suspend (items: List<T>) -> Unit
): Flow<PagingData<T>> = Pager(
    config = PagingConfig(
        pageSize = DEFAULT_PAGE_SIZE,
        prefetchDistance = DEFAULT_PAGE_SIZE / PREFETCH_DIVIDER,
        initialLoadSize = DEFAULT_PAGE_SIZE,
        enablePlaceholders = true
    ),
    remoteMediator = ListRemoteMediator<E, T>(
        listKey = listKey,
        ttl = ttl,
        loader = this,
        dispatchers = dispatchers,
        listDao = listDao,
        fetch = fetch,
        itemId = itemId,
        saveItems = saveItems
    ),
    pagingSourceFactory = { OpenEndPagingSource(pagingSource()) { listDao.isEnded(listKey) == true } }
).flow.map { data -> data.map(toDomain) }

private const val PREFETCH_DIVIDER = 3