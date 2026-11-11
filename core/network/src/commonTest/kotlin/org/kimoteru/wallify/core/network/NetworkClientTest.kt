package org.kimoteru.wallify.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.common.response.DetailedResponse
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseErrorType
import org.kimoteru.wallify.core.network.client.HttpClientFactory
import org.kimoteru.wallify.core.network.config.StaticNetworkConfig
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl
import org.kimoteru.wallify.core.network.response.executeRequest
import org.kimoteru.wallify.core.network.response.executeRequestWithErrorCode
import org.kimoteru.wallify.core.network.session.SessionExpiryCollector
import org.kimoteru.wallify.core.network.session.SessionExpiryCollectorImpl
import org.kimoteru.wallify.core.network.stats.RequestStatsCollectorImpl

class NetworkClientTest {

    @Serializable
    private data class PhotoDto(val id: String)

    @Test
    fun staticHeadersAreSent() = runBlocking {
        val client = factory().create(
            engine = MockEngine { request ->
                assertEquals("Client-ID abc", request.headers[HttpHeaders.Authorization])
                assertEquals("v1", request.headers["Accept-Version"])
                assertEquals("api.example.com", request.url.host)
                respond(content = """{"id":"p1"}""", headers = jsonHeaders())
            }
        )
        val result = executeRequest<PhotoDto> { client.get("photos/p1") }
        assertEquals(Response.Success(PhotoDto("p1")), result)
        client.close()
    }

    @Test
    fun sessionHeadersOverrideClientIdAndSetLanguage() = runBlocking {
        val collector = HeadersCollectorRepositoryImpl()
        val client = factory(collector).create(
            engine = MockEngine { request ->
                assertEquals("Bearer user-token", request.headers[HttpHeaders.Authorization])
                assertEquals("en-US", request.headers[HttpHeaders.AcceptLanguage])
                respond(content = """{"id":"p1"}""", headers = jsonHeaders())
            }
        )
        collector.updateAuthorization("Bearer user-token")
        collector.updateLanguageTag("en-US")
        assertIs<Response.Success<PhotoDto>>(executeRequest<PhotoDto> { client.get("me") })
        client.close()
    }

    @Test
    fun errorsListBecomesResponseError() = runBlocking {
        val client = factory().create(
            engine = MockEngine {
                respond(
                    content = """{"errors":["OAuth error: The access token is invalid"]}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = jsonHeaders()
                )
            }
        )
        val error = assertIs<Response.Error>(executeRequest<PhotoDto> { client.get("me") })
        assertEquals(401, error.errorData.code)
        assertEquals("OAuth error: The access token is invalid", error.errorData.message)
        client.close()
    }

    @Test
    fun exhaustedRequestLimitIsRateLimited() = runBlocking {
        val client = factory().create(
            engine = MockEngine {
                respond(
                    content = "Rate Limit Exceeded",
                    status = HttpStatusCode.Forbidden,
                    headers = headersOf("X-Ratelimit-Remaining", "0")
                )
            }
        )
        val error = assertIs<Response.Error>(executeRequest<PhotoDto> { client.get("photos") })
        assertEquals(403, error.errorData.code)
        assertEquals(ResponseErrorType.RateLimited, error.errorData.type)
        client.close()
    }

    @Test
    fun forbiddenWithRequestsLeftIsForbidden() = runBlocking {
        val client = factory().create(
            engine = MockEngine {
                respond(
                    content = """{"errors":["Forbidden"]}""",
                    status = HttpStatusCode.Forbidden,
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
                        "X-Ratelimit-Remaining" to listOf("12")
                    )
                )
            }
        )
        val error = assertIs<Response.Error>(executeRequest<PhotoDto> { client.get("photos") })
        assertEquals(ResponseErrorType.Forbidden, error.errorData.type)
        client.close()
    }

    @Test
    fun notFoundKeepsStatusAndType() = runBlocking {
        val client = factory().create(
            engine = MockEngine {
                respond(
                    content = """{"errors":["Couldn't find Photo"]}""",
                    status = HttpStatusCode.NotFound,
                    headers = jsonHeaders()
                )
            }
        )
        val error = assertIs<Response.Error>(executeRequest<PhotoDto> { client.get("photos/x") })
        assertEquals(404, error.errorData.code)
        assertEquals(ResponseErrorType.NotFound, error.errorData.type)
        client.close()
    }

    @Test
    fun errorCodeVariantReturnsStatus() = runBlocking {
        val client = factory().create(
            engine = MockEngine {
                respond(content = "", status = HttpStatusCode.Forbidden, headers = jsonHeaders())
            }
        )
        val result = executeRequestWithErrorCode<PhotoDto> { client.get("photos") }
        assertEquals(DetailedResponse.Error(403), result)
        client.close()
    }

    @Test
    fun unauthorizedWithTheUserTokenIsReportedAsAnExpiredSession() = runBlocking {
        val collector = HeadersCollectorRepositoryImpl()
        val sessionExpiry = RecordingSessionExpiryCollector()
        val client = factory(collector, sessionExpiry).create(engine = unauthorizedEngine())
        collector.updateAuthorization("Bearer user-token")

        executeRequest<PhotoDto> { client.get("me") }

        assertEquals(listOf("Bearer user-token"), sessionExpiry.rejected)
        client.close()
    }

    @Test
    fun unauthorizedOfTheAnonymousClientIsNotAnExpiredSession() = runBlocking {
        val sessionExpiry = RecordingSessionExpiryCollector()
        val client = factory(sessionExpiry = sessionExpiry).create(engine = unauthorizedEngine())

        executeRequest<PhotoDto> { client.get("me") }

        assertTrue(sessionExpiry.rejected.isEmpty())
        client.close()
    }

    @Test
    fun unauthorizedOfTheTokenExchangeIsNotAnExpiredSession() = runBlocking {
        val collector = HeadersCollectorRepositoryImpl()
        val sessionExpiry = RecordingSessionExpiryCollector()
        val client = factory(collector, sessionExpiry).create(engine = unauthorizedEngine())
        collector.updateAuthorization("Bearer user-token")

        executeRequest<PhotoDto> { client.post("https://unsplash.com/oauth/token") }

        assertTrue(sessionExpiry.rejected.isEmpty())
        client.close()
    }

    @Test
    fun otherFailuresWithTheUserTokenAreNotAnExpiredSession() = runBlocking {
        val collector = HeadersCollectorRepositoryImpl()
        val sessionExpiry = RecordingSessionExpiryCollector()
        val client = factory(collector, sessionExpiry).create(
            engine = MockEngine { respond(content = "", status = HttpStatusCode.Forbidden, headers = jsonHeaders()) }
        )
        collector.updateAuthorization("Bearer user-token")

        executeRequest<PhotoDto> { client.get("me") }

        assertTrue(sessionExpiry.rejected.isEmpty())
        client.close()
    }
}

private class RecordingSessionExpiryCollector : SessionExpiryCollector {
    val rejected = mutableListOf<String>()

    override val rejectedAuthorizations: Flow<String> = emptyFlow()

    override fun onRejected(authorization: String) {
        rejected += authorization
    }
}

private fun unauthorizedEngine() = MockEngine {
    respond(
        content = """{"errors":["OAuth error: The access token is invalid"]}""",
        status = HttpStatusCode.Unauthorized
    )
}

private fun factory(
    collector: HeadersCollectorRepositoryImpl = HeadersCollectorRepositoryImpl(),
    sessionExpiry: SessionExpiryCollector = SessionExpiryCollectorImpl(),
) = HttpClientFactory(
    networkConfig = StaticNetworkConfig(
        baseUrl = "https://api.example.com/",
        defaultHeaders = mapOf(
            "Accept-Version" to "v1",
            "Authorization" to "Client-ID abc"
        )
    ),
    headersCollector = collector,
    requestStatsCollector = RequestStatsCollectorImpl(),
    sessionExpiryCollector = sessionExpiry
)

private fun jsonHeaders() = headersOf(
    HttpHeaders.ContentType,
    ContentType.Application.Json.toString()
)