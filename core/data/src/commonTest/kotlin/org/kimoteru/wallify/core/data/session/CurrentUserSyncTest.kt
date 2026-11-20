package org.kimoteru.wallify.core.data.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentUserSyncTest {

    private val headers = HeadersCollectorRepositoryImpl()

    @Test
    fun tokenThatReachesTheClientLoadsTheUser() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUsers(Response.Success(USER))
        val sync = createSync(users, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")

        assertEquals(1, users.refreshCalls)
        assertEquals(USER, users.current)
        assertEquals(null, sync.loadError.value)
    }

    @Test
    fun tokenIsNotUsedBeforeTheClientSendsIt() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUsers(Response.Success(USER))

        createSync(users, session = FakeSessionRepository(token = "token"), scope = backgroundScope)

        assertEquals(0, users.refreshCalls)
    }

    @Test
    fun eachTokenLoadsTheUserForItsOwnSession() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUsers(Response.Success(USER))
        createSync(users, scope = backgroundScope)

        headers.updateAuthorization("Bearer first")
        headers.updateAuthorization("Bearer second")

        assertEquals(listOf("Bearer first", "Bearer second"), users.refreshedAuthorizations)
    }

    @Test
    fun lostTokenClearsTheUser() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUsers(Response.Success(USER))
        createSync(users, scope = backgroundScope)
        headers.updateAuthorization("Bearer token")

        headers.updateAuthorization(null)

        assertEquals(null, users.current)
    }

    @Test
    fun startWithoutTokenYetKeepsTheSavedUserWhileTheStorageHoldsOne() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUsers(Response.Success(USER))

        createSync(users, session = FakeSessionRepository(token = "token"), scope = backgroundScope)

        assertEquals(0, users.clearCalls)
    }

    @Test
    fun rejectedUserLoadReportsNoError() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUsers(Response.Error(ResponseError(message = "Unauthorized", code = 401)))
        val sync = createSync(users, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")

        assertEquals(1, users.refreshCalls)
        assertEquals(null, sync.loadError.value)
    }

    @Test
    fun retryOfTheUserLoadsAgainWithoutWaitingForTheConnection() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUsers(SERVER_ERROR, Response.Success(USER))
        val sync = createSync(users, scope = backgroundScope)
        headers.updateAuthorization("Bearer token")
        assertEquals(1, users.refreshCalls)

        sync.retry()

        assertEquals(2, users.refreshCalls)
        assertEquals(USER, users.current)
        assertEquals(null, sync.loadError.value)
    }

    @Test
    fun otherErrorsAreReported() = runTest(UnconfinedTestDispatcher()) {
        val sync = createSync(FakeUsers(SERVER_ERROR), scope = backgroundScope)

        headers.updateAuthorization("Bearer token")

        assertEquals(SERVER_ERROR.errorData, sync.loadError.value)
    }

    @Test
    fun failureOfflineIsRepeatedWhenConnectionReturns() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivityRepository(initial = false)
        val users = FakeUsers(SERVER_ERROR, Response.Success(USER))
        val sync = createSync(users, connectivity = connectivity, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")
        assertEquals(1, users.refreshCalls)
        assertNotNull(sync.loadError.value)

        connectivity.setOnline(true)

        assertEquals(2, users.refreshCalls)
        assertEquals(USER, users.current)
        assertEquals(null, sync.loadError.value)
    }

    @Test
    fun failureWhileOnlineWaitsForTheNextReturnOfTheConnection() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivityRepository(initial = true)
        val users = FakeUsers(SERVER_ERROR, Response.Success(USER))
        createSync(users, connectivity = connectivity, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")
        assertEquals(1, users.refreshCalls)

        connectivity.setOnline(false)
        assertEquals(1, users.refreshCalls)

        connectivity.setOnline(true)
        assertEquals(2, users.refreshCalls)
    }

    private fun createSync(
        users: FakeUsers,
        session: FakeSessionRepository = FakeSessionRepository(),
        connectivity: FakeConnectivityRepository = FakeConnectivityRepository(initial = true),
        scope: CoroutineScope
    ) = CurrentUserSync(
        headersCollector = headers,
        session = session,
        connectivityRepository = connectivity,
        refresh = users::refresh,
        clear = users::clear
    ).also { it.start(scope) }
}

private val USER = User(
    id = "u1",
    username = "jimmy",
    name = "James Example",
    firstName = null,
    lastName = null,
    bio = null,
    location = null,
    portfolioUrl = null,
    instagramUsername = null,
    email = null,
    totalPhotos = 0,
    totalCollections = 0,
    profileImage = null,
    profileUrl = null
)

private val SERVER_ERROR = Response.Error(ResponseError(message = "Server error", code = 500))

/** Answers the refreshes one by one with [results]; the last answer repeats. */
private class FakeUsers(vararg results: Response<User>) {
    private val answers = results.toList()

    var current: User? = null
        private set

    var refreshCalls = 0
        private set

    var clearCalls = 0
        private set

    val refreshedAuthorizations = mutableListOf<String>()

    suspend fun refresh(authorization: String): Response<User> {
        val answer = answers[minOf(refreshCalls, answers.lastIndex)]
        refreshCalls++
        refreshedAuthorizations += authorization
        if (answer is Response.Success) current = answer.data
        return answer
    }

    fun clear() {
        clearCalls++
        current = null
    }
}

internal class FakeSessionRepository(token: String? = null) : SessionRepository {
    private val state = MutableStateFlow(token)

    override val authToken: Flow<String?> = state

    override suspend fun updateAuthToken(token: String?) {
        state.value = token
    }

    override suspend fun clear() {
        state.value = null
    }
}

private class FakeConnectivityRepository(initial: Boolean) : ConnectivityRepository {
    override val isOnline: StateFlow<Boolean>
        field = MutableStateFlow(initial)

    fun setOnline(value: Boolean) {
        isOnline.value = value
    }

    override fun start() = Unit

    override fun stop() = Unit
}