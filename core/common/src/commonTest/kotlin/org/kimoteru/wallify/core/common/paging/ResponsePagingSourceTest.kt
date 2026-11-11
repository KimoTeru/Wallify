package org.kimoteru.wallify.core.common.paging

import androidx.paging.PagingSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.ResponseErrorType

class ResponsePagingSourceTest {
    private fun refresh(key: Int?, loadSize: Int) = PagingSource.LoadParams.Refresh(
        key = key,
        loadSize = loadSize,
        placeholdersEnabled = false
    )

    @Test
    fun fullPageHasNextKey() = runTest {
        val source = ResponsePagingSource<Int> { page, size -> Response.Success(List(size) { page * 100 + it }) }

        val page = assertIs<PagingSource.LoadResult.Page<Int, Int>>(source.load(refresh(key = null, loadSize = 3)))

        assertEquals(listOf(100, 101, 102), page.data)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun shortPageIsLast() = runTest {
        val source = ResponsePagingSource<Int> { _, _ -> Response.Success(listOf(1)) }

        val page = assertIs<PagingSource.LoadResult.Page<Int, Int>>(source.load(refresh(key = 3, loadSize = 3)))

        assertEquals(2, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun errorBecomesLoadError() = runTest {
        val source = ResponsePagingSource<Int> { _, _ -> Response.Error(ResponseError("boom", 500)) }

        val result = assertIs<PagingSource.LoadResult.Error<Int, Int>>(source.load(refresh(key = null, loadSize = 3)))

        val error = assertIs<PagingLoadException>(result.throwable)
        assertEquals("boom", error.message)
        assertEquals(500, error.code)
        assertEquals(ResponseErrorType.Server, error.type)
    }

    @Test
    fun thrownExceptionBecomesLoadError() = runTest {
        val source = ResponsePagingSource<Int> { _, _ -> error("network down") }

        val result = assertIs<PagingSource.LoadResult.Error<Int, Int>>(source.load(refresh(key = null, loadSize = 3)))

        assertEquals("network down", result.throwable.message)
    }
}