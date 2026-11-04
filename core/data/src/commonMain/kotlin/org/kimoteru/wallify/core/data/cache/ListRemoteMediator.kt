package org.kimoteru.wallify.core.data.cache

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.CancellationException
import org.kimoteru.wallify.core.common.paging.FIRST_PAGE
import org.kimoteru.wallify.core.common.paging.PagingLoadException
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.entity.ListStateEntity
import kotlin.time.Duration

/**
 * Fills the database for a paged list that the screen reads from Room (the list [listKey]).
 *
 * - Opening the list: a list younger than [ttl] is shown as it is, an older one (or one never loaded) is refreshed.
 *   The screen reads the list from the database only; what [fetch] returns reaches it through [saveItems].
 * - Refresh: the first page replaces the head of the saved list, so the list on screen stays until the new data lands.
 *   If the backend fails, the saved list stays and Paging reports the failure through `LoadState.refresh`.
 * - Append: the next page is derived from how many items are saved, so it works after a restart too.
 * - The last page is the first one shorter than requested.
 */
@OptIn(ExperimentalPagingApi::class)
internal class ListRemoteMediator<E : Any, T : Any>(
    private val listKey: String,
    private val ttl: Duration,
    private val loader: CacheLoader,
    private val listDao: ListDao,
    private val fetch: suspend (page: Int, size: Int) -> Response<List<T>>,
    private val itemId: (T) -> String,
    private val saveItems: suspend (items: List<T>) -> Unit
) : RemoteMediator<Int, E>() {

    override suspend fun initialize(): InitializeAction {
        val updatedAt = listDao.getUpdatedAt(listKey)
        val isFresh = updatedAt != null && loader.isFresh(updatedAt, ttl)
        return if (isFresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, E>): MediatorResult {
        if (loadType == LoadType.PREPEND) return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val pageSize = state.config.pageSize
            val isRefresh = loadType == LoadType.REFRESH
            val page = if (isRefresh) FIRST_PAGE else listDao.countEntries(listKey) / pageSize + FIRST_PAGE

            when (val response = fetch(page, pageSize)) {
                is Response.Error -> MediatorResult.Error(
                    PagingLoadException(message = response.errorData.message, code = response.errorData.code)
                )
                is Response.Success -> {
                    val items = response.data
                    val now = loader.now()
                    saveItems(items)
                    listDao.putEntries(
                        listKey = listKey,
                        itemIds = items.map(itemId),
                        startPosition = (page - FIRST_PAGE) * pageSize,
                        isRefresh = isRefresh
                    )
                    if (isRefresh) listDao.upsertState(ListStateEntity(listKey = listKey, updatedAtEpochMillis = now))
                    MediatorResult.Success(endOfPaginationReached = items.size < pageSize)
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }
}