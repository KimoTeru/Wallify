package org.kimoteru.wallify.core.data.cache

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.paging.FIRST_PAGE
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseException
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.entity.ListStateEntity
import kotlin.time.Duration

/**
 * Fills the database for a paged list that the screen reads from Room (the list [listKey]).
 *
 * - Opening the list: a list younger than [ttl] is shown as it is, an older one (or one never loaded) is refreshed.
 *   The screen reads the list from the database only; what [fetch] returns reaches it through [saveItems].
 * - Refresh: the first page replaces the head of the saved list, so the list on screen stays until the new data lands.
 *   If the backend fails, the saved list stays and Paging reports the failure through `LoadState.refresh`. A refresh
 *   of the user (`LazyPagingItems.refresh()`) always runs, whatever the age of the list.
 * - Append: the next page is derived from how many items are saved, so it works after a restart too. Paging asks for it
 *   only when the user scrolls near the end ([OpenEndPagingSource]), so a refresh costs one request.
 * - A request of a source that has not seen all saved rows (it read the table before the last write: Paging queues the
 *   APPEND of an empty source while the REFRESH still runs, or of a source reloaded between two writes of a page) is
 *   answered without a request: a newer source reads those rows, because the write that added them invalidated the
 *   old one, and it asks again only if the user reaches its end.
 * - The last page is the first one shorter than requested (possibly an empty one); it is saved as the end of the list
 *   (`ListStateEntity.isEnded`) and a refresh writes the flag anew, so a list saved to its end asks for nothing more.
 */
@OptIn(ExperimentalPagingApi::class)
internal class ListRemoteMediator<E : Any, T : Any>(
    private val listKey: String,
    private val ttl: Duration,
    private val loader: CacheLoader,
    private val dispatchers: AppDispatchers,
    private val listDao: ListDao,
    private val fetch: suspend (page: Int, size: Int) -> Response<List<T>>,
    private val itemId: (T) -> String,
    private val saveItems: suspend (items: List<T>) -> Unit,
) : RemoteMediator<Int, E>() {

    override suspend fun initialize(): InitializeAction {
        val isFresh = loader.isFresh(listKey, ttl)
        return if (isFresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    // Paging runs the mediator in the scope that collects the list (`cachedIn` on the main thread of the screen);
    // the request, the decoding of the page and its saving go to `io`, or every page loaded during a scroll drops
    // frames.
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, E>,
    ): MediatorResult = withContext(dispatchers.io) {
        loadPage(loadType, state)
    }

    private suspend fun loadPage(loadType: LoadType, state: PagingState<Int, E>): MediatorResult {
        if (loadType == LoadType.PREPEND) return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val pageSize = state.config.pageSize
            val isRefresh = loadType == LoadType.REFRESH
            if (!isRefresh && listDao.countEntries(listKey) > state.loadedCount()) {
                return MediatorResult.Success(endOfPaginationReached = false)
            }
            val page = if (isRefresh) FIRST_PAGE else nextPage(pageSize)
            if (page == null) return MediatorResult.Success(endOfPaginationReached = true)

            when (val response = fetch(page, pageSize)) {
                is Response.Error -> MediatorResult.Error(ResponseException(response.errorData))

                is Response.Success -> {
                    val items = response.data
                    val now = loader.now()
                    val isEnded = items.size < pageSize
                    saveItems(items)
                    listDao.putEntries(
                        listKey = listKey,
                        itemIds = items.map(itemId),
                        startPosition = (page - FIRST_PAGE) * pageSize,
                        isRefresh = isRefresh
                    )
                    // After the rows: a failed save must not mark the list fresh. A source reloaded by the rows may
                    // read the old flag for a moment; nextPage() reads the new one, so no request goes out.
                    if (isRefresh) {
                        listDao.upsertState(ListStateEntity(listKey, updatedAtEpochMillis = now, isEnded = isEnded))
                    } else if (isEnded) {
                        listDao.setEnded(listKey, isEnded = true)
                    }
                    MediatorResult.Success(endOfPaginationReached = isEnded)
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    /**
     * Rows the source that sent the request has seen: `itemsBefore` of its first page (the offset of the page; the
     * pager has placeholders on, so Room fills it; undefined counts as 0) plus the items of its pages.
     */
    private fun PagingState<Int, E>.loadedCount(): Int {
        val before = pages.firstOrNull()?.itemsBefore?.takeIf { it != PagingSource.LoadResult.Page.COUNT_UNDEFINED }
        return (before ?: 0) + pages.sumOf { it.data.size }
    }

    /**
     * The page after the saved ones, or `null` when the saved list already reached its end: the backend answered with
     * a short page (the flag of `list_state`, which also covers a list that ends exactly on a page border and an empty
     * one), or the count is not a whole number of pages. The end is read from the database, so it holds for a new
     * pager too (another opening of the screen), and the last page is not asked for again.
     */
    private suspend fun nextPage(pageSize: Int): Int? {
        val savedCount = listDao.countEntries(listKey)
        val isComplete = listDao.isEnded(listKey) == true || savedCount % pageSize != 0

        return if (isComplete) null else savedCount / pageSize + FIRST_PAGE
    }
}