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

/** Pages read from Room at once after the source is invalidated; the default of Paging. */
private const val ROOM_LOAD_PAGES = 3

/**
 * The next page is asked for this many items before the end. With a whole page (the default of Paging) a list of one
 * page asks for the second one at once, before the user scrolls: every opened list cost two requests.
 */
private const val PREFETCH_DISTANCE = DEFAULT_PAGE_SIZE / 3

/**
 * Paged list that the screen reads from Room: [pagingSource] reads the saved rows ([E]) in the order of the list
 * [listKey], [ListRemoteMediator] fills them from [fetch], and [toDomain] gives the rows to the layers above.
 * Collect it in the presentation layer with `cachedIn(scope)`. The mediator loads, decodes and saves the pages on
 * `dispatchers.io` of the repository, not on the main thread that collects the list.
 *
 * `initialLoadSize` is three pages: it is the size of a read from Room only, the mediator asks the backend by
 * `pageSize`, so its page arithmetic stays correct. Every saved page invalidates the source, and the reloaded one reads
 * `initialLoadSize` rows around the anchor; with one page some visible cards would turn into placeholders of another
 * height, and the staggered grid would lay its lanes out again (the list jumps).
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
        prefetchDistance = PREFETCH_DISTANCE,
        initialLoadSize = DEFAULT_PAGE_SIZE * ROOM_LOAD_PAGES,
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
    pagingSourceFactory = pagingSource
).flow.map { data -> data.map(toDomain) }