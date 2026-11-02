package org.kimoteru.wallify.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.common.response.DetailedResponse
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.network.client.HttpClientFactory
import org.kimoteru.wallify.core.network.config.StaticNetworkConfig
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl
import org.kimoteru.wallify.core.network.response.executeRequest
import org.kimoteru.wallify.core.network.response.executeRequestWithErrorCode

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
}

private fun factory(
    collector: HeadersCollectorRepositoryImpl = HeadersCollectorRepositoryImpl()
) = HttpClientFactory(
    networkConfig = StaticNetworkConfig(
        baseUrl = "https://api.example.com/",
        defaultHeaders = mapOf(
            "Accept-Version" to "v1",
            "Authorization" to "Client-ID abc"
        )
    ),
    headersCollector = collector
)

private fun jsonHeaders() = headersOf(
    HttpHeaders.ContentType,
    ContentType.Application.Json.toString()
)