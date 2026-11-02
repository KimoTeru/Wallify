package org.kimoteru.wallify.core.common.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Response

/**
 * Paged list of [T] loaded page by page through [loader]. Collect it in the presentation layer with
 * `cachedIn(scope)`, and show it with `collectAsLazyPagingItems()`.
 */
fun <T : Any> createPager(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    prefetchDistance: Int = pageSize,
    enablePlaceholders: Boolean = false,
    loader: suspend (page: Int, loadSize: Int) -> Response<List<T>>
): Flow<PagingData<T>> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        initialLoadSize = pageSize,
        enablePlaceholders = enablePlaceholders
    ),
    initialKey = FIRST_PAGE,
    pagingSourceFactory = { ResponsePagingSource(loader) }
).flow