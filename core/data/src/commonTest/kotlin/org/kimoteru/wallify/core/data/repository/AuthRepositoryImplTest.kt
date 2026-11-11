package org.kimoteru.wallify.core.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.data.cache.FakeAppDispatchers
import org.kimoteru.wallify.core.data.remote.AuthRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.model.auth.AuthorizationRedirect
import org.kimoteru.wallify.core.platform.managers.auth.AuthBrowser

class AuthRepositoryImplTest {

    @Test
    fun signInKeepsTokenAndMarksUserSignedIn() = runTest {
        val session = FakeSessionRepository()
        val browser = FakeAuthBrowser(redirectedTo = "$REDIRECT?code=code-1")
        val repository = repository(browser = browser, session = session)

        val result = repository.signIn()

        assertEquals(Response.Success(true), result)
        assertEquals("token-for-code-1", session.authToken.first())
        assertEquals(true, repository.isSignedIn.first())
        assertEquals("https://example.test/authorize?redirect=$REDIRECT", browser.openedUrl)
    }

    @Test
    fun declinedSignInIsNotAnErrorAndKeepsPreviousSession() = runTest {
        val session = FakeSessionRepository(token = "old")
        val browser = FakeAuthBrowser(redirectedTo = "$REDIRECT?error=access_denied")

        val result = repository(browser = browser, session = session).signIn()

        assertEquals(Response.Success(false), result)
        assertEquals("old", session.authToken.first())
    }

    @Test
    fun userWhoDoesNotComeBackIsNotSignedIn() = runTest {
        val session = FakeSessionRepository()
        val browser = FakeAuthBrowser(redirectedTo = null)

        val result = repository(browser = browser, session = session).signIn()

        assertEquals(Response.Success(false), result)
        assertEquals(null, session.authToken.first())
    }

    @Test
    fun browserThatCannotBeOpenedBecomesError() = runTest {
        val browser = FakeAuthBrowser(redirectedTo = null, failure = IllegalStateException("No browser"))

        val result = repository(browser = browser).signIn()

        assertEquals("No browser", assertIs<Response.Error>(result).errorData.message)
    }

    @Test
    fun codeRefusedByBackendBecomesErrorAndKeepsPreviousSession() = runTest {
        val session = FakeSessionRepository(token = "old")
        val browser = FakeAuthBrowser(redirectedTo = "$REDIRECT?code=bad")
        val refused = Response.Error(ResponseError(message = "invalid_grant", code = 400))

        val result = repository(browser = browser, session = session, tokenResponse = refused).signIn()

        assertIs<Response.Error>(result)
        assertEquals("old", session.authToken.first())
    }

    @Test
    fun signOutClearsSession() = runTest {
        val session = FakeSessionRepository(token = "old")
        val repository = repository(session = session)

        repository.signOut()

        assertEquals(null, session.authToken.first())
        assertEquals(false, repository.isSignedIn.first())
    }

    @Test
    fun cancellationIsNotSwallowed() = runTest {
        val browser = FakeAuthBrowser(redirectedTo = null, failure = CancellationException("stop"))

        assertFailsWith<CancellationException> { repository(browser = browser).signIn() }
    }

    private fun repository(
        browser: FakeAuthBrowser = FakeAuthBrowser(redirectedTo = null),
        session: FakeSessionRepository = FakeSessionRepository(),
        tokenResponse: Response<String>? = null
    ) = AuthRepositoryImpl(
        FakeAuthRemoteDataSource(tokenResponse),
        browser,
        session,
        FakeAppDispatchers()
    )
}

private const val REDIRECT = "wallify://oauth/callback"

private class FakeSessionRepository(token: String? = null) : SessionRepository {
    private val state = MutableStateFlow(token)

    override val authToken: Flow<String?> = state

    override suspend fun updateAuthToken(token: String?) {
        state.value = token
    }

    override suspend fun clear() {
        state.value = null
    }
}

private class FakeAuthBrowser(
    private val redirectedTo: String?,
    private val failure: Throwable? = null
) : AuthBrowser {
    var openedUrl: String? = null

    override val redirectUri: String = REDIRECT

    override suspend fun open(authorizationUrl: String): String? {
        openedUrl = authorizationUrl
        failure?.let { throw it }
        return redirectedTo
    }
}

/** Answers like the backend: `code=<x>` grants, `error=…` declines, the token is built from the code. */
private class FakeAuthRemoteDataSource(private val tokenResponse: Response<String>?) : AuthRemoteDataSource {

    override fun getAuthorizationUrl(redirectUri: String): String =
        "https://example.test/authorize?redirect=$redirectUri"

    override fun parseRedirect(url: String, redirectUri: String): AuthorizationRedirect? = when {
        !url.startsWith(redirectUri) -> null
        "code=" in url -> AuthorizationRedirect.Granted(code = url.substringAfter("code="))
        else -> AuthorizationRedirect.Denied(reason = "access_denied")
    }

    override suspend fun requestToken(code: String, redirectUri: String): Response<String> =
        tokenResponse ?: Response.Success("token-for-$code")
}