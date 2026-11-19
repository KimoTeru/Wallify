package org.kimoteru.wallify.core.data.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl
import org.kimoteru.wallify.core.network.session.SessionExpiryCollectorImpl

@OptIn(ExperimentalCoroutinesApi::class)
class SessionExpiryHandlerTest {

    private val headers = HeadersCollectorRepositoryImpl()

    private val sessionExpiry = SessionExpiryCollectorImpl()

    @Test
    fun tokenRejectedByBackendEndsTheSession() = runTest(UnconfinedTestDispatcher()) {
        val session = FakeSessionRepository(token = "token")
        SessionExpiryHandler(headers, sessionExpiry, session, backgroundScope).start()
        headers.updateAuthorization("Bearer token")

        sessionExpiry.onRejected("Bearer token")

        assertEquals(null, session.authToken.first())
    }

    @Test
    fun lateRejectionOfAnOldTokenKeepsTheNewSession() = runTest(UnconfinedTestDispatcher()) {
        val session = FakeSessionRepository(token = "new")
        SessionExpiryHandler(headers, sessionExpiry, session, backgroundScope).start()
        headers.updateAuthorization("Bearer new")

        sessionExpiry.onRejected("Bearer old")

        assertEquals("new", session.authToken.first())
    }
}