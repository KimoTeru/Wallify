package org.kimoteru.wallify.core.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.response.Response
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class CacheTtlTest {

    private val time = FakeTimeProvider(now = 1_000_000L)
    private val listDao = FakeListDao()
    private val cacheLoader = CacheLoader(listDao = listDao, time = time)
    private val database = MutableStateFlow<String?>(null)
    private val databaseList = MutableStateFlow<List<String>>(emptyList())
    private var requests = 0

    private fun item(ttl: Duration) = refreshableFlow(
        loader = cacheLoader,
        dispatchers = FakeAppDispatchers(),
        key = "item:photo",
        ttl = ttl,
        roomFlow = { database },
        remoteCall = {
            requests++
            Response.Success("remote")
        },
        save = { remote -> database.value = remote },
        toDomain = { it }
    )

    private fun list(ttl: Duration) = refreshableListFlow(
        loader = cacheLoader,
        dispatchers = FakeAppDispatchers(),
        key = "collections:user:test",
        ttl = ttl,
        roomFlow = { databaseList },
        remoteCall = {
            requests++
            Response.Success(listOf("remote"))
        },
        save = { remote -> databaseList.value = remote },
        toDomain = { it }
    )

    @Test
    fun `ttl of a photo is a week and ttl of other items stays a day`() {
        assertEquals(7.days, CacheTtl.Photo)
        assertEquals(1.days, CacheTtl.Item)
        assertTrue(CacheTtl.Photo < CacheTtl.Retention)
    }

    @Test
    fun `photo is not requested again within a week but is after it`() = runTest {
        backgroundScope.launch { item(CacheTtl.Photo).toList(mutableListOf()) }
        runCurrent()

        time.now += 6.days.inWholeMilliseconds
        backgroundScope.launch { item(CacheTtl.Photo).toList(mutableListOf()) }
        runCurrent()
        assertEquals(1, requests)

        time.now += 1.days.inWholeMilliseconds + 1
        backgroundScope.launch { item(CacheTtl.Photo).toList(mutableListOf()) }
        runCurrent()
        assertEquals(2, requests)
    }

    @Test
    fun `collections of the user are not requested again within a day but are after it`() = runTest {
        assertEquals(1.days, CacheTtl.UserCollections)
        backgroundScope.launch { list(CacheTtl.UserCollections).toList(mutableListOf()) }
        runCurrent()

        time.now += 23 * 60 * 60 * 1000L
        backgroundScope.launch { list(CacheTtl.UserCollections).toList(mutableListOf()) }
        runCurrent()
        assertEquals(1, requests)

        time.now += 2 * 60 * 60 * 1000L
        backgroundScope.launch { list(CacheTtl.UserCollections).toList(mutableListOf()) }
        runCurrent()
        assertEquals(2, requests)
    }
}