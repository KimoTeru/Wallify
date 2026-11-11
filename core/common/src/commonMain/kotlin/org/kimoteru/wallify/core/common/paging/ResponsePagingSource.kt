package org.kimoteru.wallify.core.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.ResponseErrorType

const val FIRST_PAGE = 1
const val DEFAULT_PAGE_SIZE = 30

/**
 * Page-number [PagingSource] over a loader that returns [Response].
 *
 * [loader] receives the 1-based page and the number of items to load. A page shorter than the requested size
 * is the last one. Use [createPager], which sets `initialLoadSize = pageSize`, so every request has the same size
 * and the page arithmetic stays correct.
 */
class ResponsePagingSource<T : Any>(
    private val loader: suspend (page: Int, loadSize: Int) -> Response<List<T>>
) : PagingSource<Int, T>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: FIRST_PAGE
        val loadSize = params.loadSize

        return try {
            when (val response = loader(page, loadSize)) {
                is Response.Success -> LoadResult.Page(
                    data = response.data,
                    prevKey = if (page == FIRST_PAGE) null else page - 1,
                    nextKey = if (response.data.size < loadSize) null else page + 1
                )

                is Response.Error -> LoadResult.Error(PagingLoadException(response.errorData))
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = state.getRefreshKeyOrNull()
}

/** Failure reported by a paging loader; [error] keeps the HTTP status and the [ResponseErrorType] for the screen. */
class PagingLoadException(
    val error: ResponseError,
) : Exception(error.message) {
    /** HTTP status or [ResponseError.DEBUG_ERROR_CODE]. */
    val code: Int
        get() = error.code

    val type: ResponseErrorType
        get() = error.type
}

/**
 * Error behind a failed paging load: the [ResponseError] of a [PagingLoadException], otherwise (a thrown exception)
 * an error without an HTTP status.
 */
val Throwable.responseError: ResponseError
    get() = (this as? PagingLoadException)?.error ?: ResponseError(message = message ?: "Unknown error")