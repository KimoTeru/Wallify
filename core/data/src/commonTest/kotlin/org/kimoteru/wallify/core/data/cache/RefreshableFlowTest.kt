package org.kimoteru.wallify.core.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class RefreshableFlowTest {

    private val listDao = FakeListDao()
    private val time = FakeTimeProvider(now = 1_000_000L)
    private val loader = CacheLoader(listDao = listDao, time = time)
    private val database = MutableStateFlow<String?>(null)
    private val databaseList = MutableStateFlow<List<String>>(emptyList())
    private var answer: Response<String> = Response.Success("remote")
    private var listAnswer: Response<List<String>> = Response.Success(listOf("remote"))
    private var requests = 0

    private fun item() = refreshableFlow(
        loader = loader,
        key = ITEM_KEY,
        ttl = TTL,
        roomFlow = { database },
        remoteCall = {
            requests++
            answer
        },
        save = { remote -> database.value = remote },
        toDomain = { it.uppercase() }
    )

    private fun list() = refreshableListFlow(
        loader = loader,
        key = LIST_KEY,
        ttl = TTL,
        roomFlow = { databaseList },
        remoteCall = {
            requests++
            listAnswer
        },
        save = { remote -> databaseList.value = remote },
        toDomain = { it.uppercase() }
    )

    @Test
    fun `nothing saved shows loading and then the answer of the backend through the database`() = runTest {
        val emitted = mutableListOf<Resource<String>>()
        backgroundScope.launch { item().toList(emitted) }

        runCurrent()

        assertEquals(listOf(Resource.Loading, Resource.Success("REMOTE")), emitted)
        assertEquals("remote", database.value)
    }

    @Test
    fun `saved row is shown at once and replaced by the fresh answer`() = runTest {
        database.value = "saved"
        val emitted = mutableListOf<Resource<String>>()
        backgroundScope.launch { item().toList(emitted) }

        runCurrent()

        assertEquals(listOf(Resource.Success("SAVED"), Resource.Success("REMOTE")), emitted)
    }

    @Test
    fun `failing backend keeps the saved row and reports nothing`() = runTest {
        database.value = "saved"
        answer = failure()
        val emitted = mutableListOf<Resource<String>>()
        backgroundScope.launch { item().toList(emitted) }

        runCurrent()

        assertEquals(listOf<Resource<String>>(Resource.Success("SAVED")), emitted)
    }

    @Test
    fun `failing backend without a saved row reports the error`() = runTest {
        answer = failure()
        val emitted = mutableListOf<Resource<String>>()
        backgroundScope.launch { item().toList(emitted) }

        runCurrent()

        assertEquals(listOf(Resource.Loading, Resource.Error(ERROR)), emitted)
    }

    @Test
    fun `later change of the row reaches the screen`() = runTest {
        database.value = "saved"
        val emitted = mutableListOf<Resource<String>>()
        backgroundScope.launch { item().toList(emitted) }
        runCurrent()

        database.value = "changed elsewhere"
        runCurrent()

        assertEquals(Resource.Success("CHANGED ELSEWHERE"), emitted.last())
    }

    @Test
    fun `collecting again while the item is fresh does not ask the backend`() = runTest {
        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()
        time.now += 1.minutes.inWholeMilliseconds

        val emitted = mutableListOf<Resource<String>>()
        backgroundScope.launch { item().toList(emitted) }
        runCurrent()

        assertEquals(1, requests)
        assertEquals(listOf<Resource<String>>(Resource.Success("REMOTE")), emitted)
    }

    @Test
    fun `collecting again after the ttl asks the backend again`() = runTest {
        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()
        time.now += TTL.inWholeMilliseconds + 1

        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()

        assertEquals(2, requests)
    }

    @Test
    fun `item made stale is requested again`() = runTest {
        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()
        listDao.deleteStatesByPrefix("item:")

        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()

        assertEquals(2, requests)
    }

    @Test
    fun `fresh age without a saved row still asks the backend`() = runTest {
        listDao.states[ITEM_KEY] = time.now

        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()

        assertEquals(1, requests)
    }

    @Test
    fun `failed request does not make the item fresh`() = runTest {
        database.value = "saved"
        answer = failure()
        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()

        backgroundScope.launch { item().toList(mutableListOf()) }
        runCurrent()

        assertEquals(2, requests)
    }

    @Test
    fun `list answers through the database`() = runTest {
        val emitted = mutableListOf<Resource<List<String>>>()
        backgroundScope.launch { list().toList(emitted) }

        runCurrent()

        assertEquals(listOf(Resource.Loading, Resource.Success(listOf("REMOTE"))), emitted)
    }

    @Test
    fun `empty answer of the backend is an empty list and not an endless loading`() = runTest {
        listAnswer = Response.Success(emptyList())
        val emitted = mutableListOf<Resource<List<String>>>()
        backgroundScope.launch { list().toList(emitted) }

        runCurrent()

        assertEquals(listOf(Resource.Loading, Resource.Success(emptyList())), emitted)
    }

    @Test
    fun `fresh empty list is shown at once without a request`() = runTest {
        listDao.states[LIST_KEY] = time.now
        val emitted = mutableListOf<Resource<List<String>>>()
        backgroundScope.launch { list().toList(emitted) }

        runCurrent()

        assertEquals(0, requests)
        assertEquals(listOf<Resource<List<String>>>(Resource.Success(emptyList())), emitted)
    }

    @Test
    fun `fresh saved list does not ask the backend`() = runTest {
        databaseList.value = listOf("saved")
        listDao.states[LIST_KEY] = time.now
        val emitted = mutableListOf<Resource<List<String>>>()
        backgroundScope.launch { list().toList(emitted) }

        runCurrent()

        assertEquals(0, requests)
        assertEquals(listOf<Resource<List<String>>>(Resource.Success(listOf("SAVED"))), emitted)
    }

    @Test
    fun `failing backend without a saved list reports the error`() = runTest {
        listAnswer = failure()
        val emitted = mutableListOf<Resource<List<String>>>()
        backgroundScope.launch { list().toList(emitted) }

        runCurrent()

        assertEquals(listOf(Resource.Loading, Resource.Error(ERROR)), emitted)
    }

    private fun failure(): Response.Error = Response.Error(ERROR)

    private companion object {
        const val ITEM_KEY = "item:test"
        const val LIST_KEY = "list:test"
        val TTL = 5.minutes
        val ERROR = ResponseError(message = "offline", code = ResponseError.DEBUG_ERROR_CODE)
    }
}