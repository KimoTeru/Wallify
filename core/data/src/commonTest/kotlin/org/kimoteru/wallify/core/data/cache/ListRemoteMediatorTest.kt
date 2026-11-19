package org.kimoteru.wallify.core.data.cache

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.paging.PagingLoadException
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalPagingApi::class)
class ListRemoteMediatorTest {

    private val listDao = FakeListDao()
    private val time = FakeTimeProvider(now = 1_000L)
    private val loader = CacheLoader(listDao = listDao, time = time)
    private val savedItems = mutableListOf<String>()
    private val requestedPages = mutableListOf<Int>()
    private var backend: List<String> = emptyList()
    private var failure: ResponseError? = null

    private val mediator = ListRemoteMediator<String, String>(
        listKey = LIST_KEY,
        ttl = 5.minutes,
        loader = loader,
        dispatchers = FakeAppDispatchers(),
        listDao = listDao,
        fetch = { page, size ->
            requestedPages.add(page)
            failure?.let { Response.Error(it) }
                ?: Response.Success(backend.drop((page - 1) * size).take(size))
        },
        itemId = { it },
        saveItems = { items -> savedItems.addAll(items) }
    )

    private val state = PagingState<Int, String>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = PAGE_SIZE),
        leadingPlaceholderCount = 0
    )

    @Test
    fun `list that was never loaded is refreshed on open`() = runTest {
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `fresh list is shown as it is`() = runTest {
        listDao.states[LIST_KEY] = 900L

        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `old list is refreshed on open`() = runTest {
        listDao.states[LIST_KEY] = 1_000L - 6.minutes.inWholeMilliseconds

        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `refresh saves the first page and the age of the list`() = runTest {
        backend = listOf("a", "b", "c", "d", "e")

        val result = mediator.load(LoadType.REFRESH, state)

        assertEquals(RemoteMediator.MediatorResult.Success(endOfPaginationReached = false), result)
        assertEquals(listOf("a", "b"), listDao.itemIds(LIST_KEY))
        assertEquals(listOf("a", "b"), savedItems)
        assertEquals(1_000L, listDao.states[LIST_KEY])
    }

    @Test
    fun `append loads the page after the saved ones`() = runTest {
        backend = listOf("a", "b", "c", "d", "e")
        mediator.load(LoadType.REFRESH, state)

        val result = mediator.load(LoadType.APPEND, state)

        assertEquals(RemoteMediator.MediatorResult.Success(endOfPaginationReached = false), result)
        assertEquals(listOf(1, 2), requestedPages)
        assertEquals(listOf("a", "b", "c", "d"), listDao.itemIds(LIST_KEY))
    }

    @Test
    fun `short page ends the list`() = runTest {
        backend = listOf("a", "b", "c")
        mediator.load(LoadType.REFRESH, state)

        val result = mediator.load(LoadType.APPEND, state)

        assertEquals(RemoteMediator.MediatorResult.Success(endOfPaginationReached = true), result)
        assertEquals(listOf("a", "b", "c"), listDao.itemIds(LIST_KEY))
    }

    @Test
    fun `append of an already complete list does not duplicate the last page`() = runTest {
        backend = listOf("a", "b", "c")
        mediator.load(LoadType.REFRESH, state)
        mediator.load(LoadType.APPEND, state)

        mediator.load(LoadType.APPEND, state)

        assertEquals(listOf("a", "b", "c"), listDao.itemIds(LIST_KEY))
    }

    @Test
    fun `refresh replaces the head and drops the old tail`() = runTest {
        backend = listOf("a", "b", "c", "d", "e")
        mediator.load(LoadType.REFRESH, state)
        mediator.load(LoadType.APPEND, state)
        mediator.load(LoadType.APPEND, state)
        backend = listOf("x")

        mediator.load(LoadType.REFRESH, state)

        assertEquals(listOf("x"), listDao.itemIds(LIST_KEY))
    }

    @Test
    fun `failed refresh keeps the saved list and reports the code`() = runTest {
        backend = listOf("a", "b")
        mediator.load(LoadType.REFRESH, state)
        failure = ResponseError(message = "offline", code = ResponseError.DEBUG_ERROR_CODE)

        val result = mediator.load(LoadType.REFRESH, state)

        val error = assertIs<RemoteMediator.MediatorResult.Error>(result)
        val cause = assertIs<PagingLoadException>(error.throwable)
        assertEquals(ResponseError.DEBUG_ERROR_CODE, cause.code)
        assertEquals(listOf("a", "b"), listDao.itemIds(LIST_KEY))
    }

    @Test
    fun `prepend has nothing to load`() = runTest {
        val result = mediator.load(LoadType.PREPEND, state)

        assertEquals(RemoteMediator.MediatorResult.Success(endOfPaginationReached = true), result)
        assertEquals(emptyList(), requestedPages)
    }

    @Test
    fun `refresh runs even when the list is fresh`() = runTest {
        backend = listOf("a", "b")
        listDao.states[LIST_KEY] = 900L

        val result = mediator.load(LoadType.REFRESH, state)

        assertEquals(RemoteMediator.MediatorResult.Success(endOfPaginationReached = false), result)
        assertEquals(listOf(1), requestedPages)
    }

    @Test
    fun `refresh with the same items does not write the list again`() = runTest {
        backend = listOf("a", "b", "c", "d", "e")
        mediator.load(LoadType.REFRESH, state)
        mediator.load(LoadType.APPEND, state)
        val writes = listDao.insertCalls

        mediator.load(LoadType.REFRESH, state)

        assertEquals(writes, listDao.insertCalls)
        assertEquals(listOf("a", "b"), listDao.itemIds(LIST_KEY).take(2))
    }

    @Test
    fun `refresh with another first page writes the list`() = runTest {
        backend = listOf("a", "b", "c")
        mediator.load(LoadType.REFRESH, state)
        val writes = listDao.insertCalls
        backend = listOf("x", "a", "b")

        mediator.load(LoadType.REFRESH, state)

        assertEquals(writes + 1, listDao.insertCalls)
        assertEquals(listOf("x", "a"), listDao.itemIds(LIST_KEY))
    }

    private companion object {
        const val LIST_KEY = "test:list"
        const val PAGE_SIZE = 2
    }
}