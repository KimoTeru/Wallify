package org.kimoteru.wallify.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.auth.AuthorizationRedirect
import org.kimoteru.wallify.core.network.api.AuthApi
import org.kimoteru.wallify.core.network.client.HttpClientFactory
import org.kimoteru.wallify.core.network.config.ApiCredentials
import org.kimoteru.wallify.core.network.config.StaticNetworkConfig
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl
import org.kimoteru.wallify.core.network.model.response.auth.TokenResponse
import org.kimoteru.wallify.core.network.session.SessionExpiryCollectorImpl
import org.kimoteru.wallify.core.network.stats.RequestStatsCollectorImpl

class AuthApiTest {

    @Test
    fun authorizationUrlAsksForCodeWithEveryScope() {
        val url = Url(api().getAuthorizationUrl(REDIRECT_URI))

        assertEquals("unsplash.com", url.host)
        assertEquals("/oauth/authorize", url.encodedPath)
        assertEquals("access", url.parameters["client_id"])
        assertEquals(REDIRECT_URI, url.parameters["redirect_uri"])
        assertEquals("code", url.parameters["response_type"])
        assertEquals(
            "public read_user write_user read_photos write_photos read_collections write_collections",
            url.parameters["scope"]
        )
    }

    @Test
    fun authorizationUrlNeverContainsSecretKey() {
        assertFalse("secret" in api().getAuthorizationUrl(REDIRECT_URI))
    }

    @Test
    fun redirectWithCodeIsGranted() {
        val redirect = api().parseRedirect("$REDIRECT_URI?code=abc123", REDIRECT_URI)

        assertEquals(AuthorizationRedirect.Granted(code = "abc123"), redirect)
    }

    @Test
    fun redirectWithErrorIsDeniedWithDescription() {
        val redirect = api().parseRedirect(
            url = "$REDIRECT_URI?error=access_denied&error_description=The+user+denied+the+request",
            redirectUri = REDIRECT_URI
        )

        assertEquals(AuthorizationRedirect.Denied(reason = "The user denied the request"), redirect)
    }

    @Test
    fun redirectWithoutCodeOrErrorIsDenied() {
        val redirect = api().parseRedirect(REDIRECT_URI, REDIRECT_URI)

        assertEquals(AuthorizationRedirect.Denied(reason = "access_denied"), redirect)
    }

    @Test
    fun redirectOfAnotherPlatformIsNotRedirect() {
        val desktopRedirect = "http://127.0.0.1:49721/oauth/callback"

        assertNull(api().parseRedirect("$desktopRedirect?code=abc123", REDIRECT_URI))
        assertEquals(
            AuthorizationRedirect.Granted(code = "abc123"),
            api().parseRedirect("$desktopRedirect?code=abc123", desktopRedirect)
        )
    }

    @Test
    fun otherAddressesAreNotRedirects() {
        assertNull(api().parseRedirect("https://unsplash.com/oauth/authorize?client_id=access", REDIRECT_URI))
        assertNull(api().parseRedirect("https://unsplash.com/login", REDIRECT_URI))
    }

    @Test
    fun tokenRequestPostsCodeWithBothKeys() = runBlocking {
        val client = client { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("unsplash.com", request.url.host)
            assertEquals("/oauth/token", request.url.encodedPath)
            val form = assertIs<FormDataContent>(request.body).formData
            assertEquals("access", form["client_id"])
            assertEquals("secret", form["client_secret"])
            assertEquals(REDIRECT_URI, form["redirect_uri"])
            assertEquals("code-1", form["code"])
            assertEquals("authorization_code", form["grant_type"])
            respond(
                content = """{"access_token":"t0","token_type":"bearer","scope":"public","created_at":1436544465}""",
                headers = jsonHeaders()
            )
        }

        val token = assertIs<Response.Success<TokenResponse>>(
            AuthApi(client, credentials()).requestToken(code = "code-1", redirectUri = REDIRECT_URI)
        )

        assertEquals("t0", token.data.accessToken)
        assertEquals(1436544465L, token.data.createdAt)
        client.close()
    }

    @Test
    fun rejectedCodeBecomesErrorWithDescription() = runBlocking {
        val client = client {
            respond(
                content = """{"error":"invalid_grant","error_description":"The provided code is invalid."}""",
                status = HttpStatusCode.BadRequest,
                headers = jsonHeaders()
            )
        }

        val error = assertIs<Response.Error>(
            AuthApi(client, credentials()).requestToken(code = "bad", redirectUri = REDIRECT_URI)
        )

        assertEquals(400, error.errorData.code)
        assertEquals("The provided code is invalid.", error.errorData.message)
        client.close()
    }

    private fun api() = AuthApi(client { respond(content = "") }, credentials())

    private fun credentials() = ApiCredentials(accessKey = "access", secretKey = "secret")
}

private const val REDIRECT_URI = "wallify://oauth/callback"

private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
    HttpClientFactory(
        networkConfig = StaticNetworkConfig(baseUrl = "https://api.example.com/"),
        headersCollector = HeadersCollectorRepositoryImpl(),
        requestStatsCollector = RequestStatsCollectorImpl(),
        sessionExpiryCollector = SessionExpiryCollectorImpl()
    ).create(engine = MockEngine(handler))

private fun jsonHeaders() = headersOf(
    HttpHeaders.ContentType,
    ContentType.Application.Json.toString()
)