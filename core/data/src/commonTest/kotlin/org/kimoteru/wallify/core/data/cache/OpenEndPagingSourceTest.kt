package org.kimoteru.wallify.core.data.cache

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpenEndPagingSourceTest {

    private var isEnded = false
    private var page = pageOf(listOf("a", "b"), nextKey = null)
    private val source = FakeSource { page }
    private val openEnd = OpenEndPagingSource(source) { isEnded }
    private val params = PagingSource.LoadParams.Refresh<Int>(key = null, loadSize = 2, placeholdersEnabled = true)

    @Test
    fun `saved rows that reach the end of the table point to the next offset`() = runTest {
        val result = openEnd.load(params)

        assertEquals(2, assertIs<PagingSource.LoadResult.Page<Int, String>>(result).nextKey)
    }

    @Test
    fun `list saved to its end keeps the end`() = runTest {
        isEnded = true

        val result = openEnd.load(params)

        assertNull(assertIs<PagingSource.LoadResult.Page<Int, String>>(result).nextKey)
    }

    @Test
    fun `empty page after the saved rows ends the source so the mediator is asked`() = runTest {
        page = pageOf(emptyList(), nextKey = null, itemsBefore = 2)

        val result = openEnd.load(params)

        assertNull(assertIs<PagingSource.LoadResult.Page<Int, String>>(result).nextKey)
    }

    @Test
    fun `page with a next key is passed as it is`() = runTest {
        page = pageOf(listOf("a", "b"), nextKey = 2)

        val result = openEnd.load(params)

        assertEquals(2, assertIs<PagingSource.LoadResult.Page<Int, String>>(result).nextKey)
    }

    @Test
    fun `invalidation of the table source invalidates the wrapper`() {
        source.invalidate()

        assertTrue(openEnd.invalid)
    }

    private fun pageOf(data: List<String>, nextKey: Int?, itemsBefore: Int = 0) =
        PagingSource.LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = nextKey,
            itemsBefore = itemsBefore,
            itemsAfter = 0
        )

    private class FakeSource(
        private val result: () -> LoadResult<Int, String>,
    ) : PagingSource<Int, String>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> = result()

        override fun getRefreshKey(state: PagingState<Int, String>): Int? = null
    }
}