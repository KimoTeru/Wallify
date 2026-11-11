package org.kimoteru.wallify.core.data.session

import androidx.paging.PagingData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl
import org.kimoteru.wallify.core.network.session.SessionExpiryCollectorImpl

@OptIn(ExperimentalCoroutinesApi::class)
class UserSessionSynchronizerTest {

    private val headers = HeadersCollectorRepositoryImpl()

    private val sessionExpiry = SessionExpiryCollectorImpl()

    @Test
    fun tokenThatReachesTheClientLoadsTheUser() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUserRepository(Response.Success(USER))
        val synchronizer = createSynchronizer(users, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")

        assertEquals(1, users.refreshCalls)
        assertEquals(USER, users.currentUser.value)
        assertEquals(null, synchronizer.loadError.value)
    }

    @Test
    fun tokenIsNotUsedBeforeTheClientSendsIt() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUserRepository(Response.Success(USER))

        createSynchronizer(users, session = RecordingSessionRepository(token = "token"), scope = backgroundScope)

        assertEquals(0, users.refreshCalls)
    }

    @Test
    fun lostTokenClearsTheUser() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUserRepository(Response.Success(USER))
        createSynchronizer(users, scope = backgroundScope)
        headers.updateAuthorization("Bearer token")

        headers.updateAuthorization(null)

        assertEquals(null, users.currentUser.value)
    }

    @Test
    fun startWithoutTokenYetKeepsTheSavedUserWhileTheStorageHoldsOne() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUserRepository(Response.Success(USER))

        createSynchronizer(users, session = RecordingSessionRepository(token = "token"), scope = backgroundScope)

        assertEquals(0, users.clearCalls)
    }

    @Test
    fun rejectedUserLoadReportsNoError() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUserRepository(Response.Error(ResponseError(message = "Unauthorized", code = 401)))
        val synchronizer = createSynchronizer(users, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")

        assertEquals(1, users.refreshCalls)
        assertEquals(null, synchronizer.loadError.value)
    }

    @Test
    fun tokenRejectedByBackendEndsTheSession() = runTest(UnconfinedTestDispatcher()) {
        val session = RecordingSessionRepository(token = "token")
        createSynchronizer(FakeUserRepository(Response.Success(USER)), session = session, scope = backgroundScope)
        headers.updateAuthorization("Bearer token")

        sessionExpiry.onRejected("Bearer token")

        assertEquals(null, session.authToken.first())
    }

    @Test
    fun lateRejectionOfAnOldTokenKeepsTheNewSession() = runTest(UnconfinedTestDispatcher()) {
        val session = RecordingSessionRepository(token = "new")
        createSynchronizer(FakeUserRepository(Response.Success(USER)), session = session, scope = backgroundScope)
        headers.updateAuthorization("Bearer new")

        sessionExpiry.onRejected("Bearer old")

        assertEquals("new", session.authToken.first())
    }

    @Test
    fun retryOfTheUserLoadsAgainWithoutWaitingForTheConnection() = runTest(UnconfinedTestDispatcher()) {
        val users = FakeUserRepository(SERVER_ERROR, Response.Success(USER))
        val synchronizer = createSynchronizer(users, scope = backgroundScope)
        headers.updateAuthorization("Bearer token")
        assertEquals(1, users.refreshCalls)

        synchronizer.retryLoad()

        assertEquals(2, users.refreshCalls)
        assertEquals(USER, users.currentUser.value)
        assertEquals(null, synchronizer.loadError.value)
    }

    @Test
    fun otherErrorsAreReportedAndKeepTheSession() = runTest(UnconfinedTestDispatcher()) {
        val session = RecordingSessionRepository(token = "token")
        val users = FakeUserRepository(SERVER_ERROR)
        val synchronizer = createSynchronizer(users, session = session, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")

        assertEquals(SERVER_ERROR.errorData, synchronizer.loadError.value)
        assertEquals("token", session.authToken.first())
    }

    @Test
    fun failureOfflineIsRepeatedWhenConnectionReturns() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivityRepository(initial = false)
        val users = FakeUserRepository(SERVER_ERROR, Response.Success(USER))
        val synchronizer = createSynchronizer(users, connectivity = connectivity, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")
        assertEquals(1, users.refreshCalls)
        assertNotNull(synchronizer.loadError.value)

        connectivity.setOnline(true)

        assertEquals(2, users.refreshCalls)
        assertEquals(USER, users.currentUser.value)
        assertEquals(null, synchronizer.loadError.value)
    }

    @Test
    fun failureWhileOnlineWaitsForTheNextReturnOfTheConnection() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivityRepository(initial = true)
        val users = FakeUserRepository(SERVER_ERROR, Response.Success(USER))
        createSynchronizer(users, connectivity = connectivity, scope = backgroundScope)

        headers.updateAuthorization("Bearer token")
        assertEquals(1, users.refreshCalls)

        connectivity.setOnline(false)
        assertEquals(1, users.refreshCalls)

        connectivity.setOnline(true)
        assertEquals(2, users.refreshCalls)
    }

    private fun createSynchronizer(
        users: FakeUserRepository,
        session: RecordingSessionRepository = RecordingSessionRepository(),
        connectivity: FakeConnectivityRepository = FakeConnectivityRepository(initial = true),
        scope: CoroutineScope
    ) = UserSessionSynchronizer(headers, sessionExpiry, users, session, connectivity, scope).also { it.start() }
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

private class RecordingSessionRepository(token: String? = null) : SessionRepository {
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

/**
 * Answers the refreshes one by one with [results]; the last answer repeats. Only the part the synchronizer uses is
 * real, the other members are never called.
 */
private class FakeUserRepository(vararg results: Response<User>) : UserRepository {
    private val answers = results.toList()

    var refreshCalls = 0
        private set

    override val currentUser: StateFlow<User?>
        field = MutableStateFlow<User?>(null)

    override suspend fun refreshCurrentUser(): Response<User> {
        val answer = answers[minOf(refreshCalls, answers.lastIndex)]
        refreshCalls++
        if (answer is Response.Success) currentUser.value = answer.data
        return answer
    }

    var clearCalls = 0
        private set

    override fun clearCurrentUser() {
        clearCalls++
        currentUser.value = null
    }

    override suspend fun updateCurrentUser(params: UpdateUserParams): Response<User> = unused()

    override val uploadPageUrl: String = ""

    override fun getUserFlow(username: String): Flow<Resource<User>> = unused()

    override suspend fun invalidateUserPhotos(username: String): Unit = unused()

    override fun getUserPhotosFlow(
        username: String,
        order: UserPhotosOrder?,
        orientation: PhotoOrientation?
    ): Flow<PagingData<Photo>> = unused()

    override fun getUserCollectionsFlow(username: String): Flow<PagingData<PhotoCollection>> = unused()

    override fun getUserCollectionListFlow(username: String): Flow<Resource<List<PhotoCollection>>> = unused()

    override fun getUserStatisticsFlow(username: String, quantity: Int?): Flow<Resource<Statistics>> = unused()

    private fun unused(): Nothing = error("Not used by the synchronizer")
}