package org.kimoteru.wallify.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.kimoteru.wallify.core.network.api.EndpointTemplates
import org.kimoteru.wallify.core.network.api.TopicsApi
import org.kimoteru.wallify.core.network.client.HttpClientFactory
import org.kimoteru.wallify.core.network.config.StaticNetworkConfig
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl
import org.kimoteru.wallify.core.network.session.SessionExpiryCollectorImpl
import org.kimoteru.wallify.core.network.stats.RequestStatsCollectorImpl

class RequestStatsTest {

    @Test
    fun idsAndNamesBecomePlaceholders() {
        assertEquals("GET /photos/{id}", EndpointTemplates.of("GET", "/photos/LBI7cgq3pbM"))
        assertEquals("GET /photos/{id}/download", EndpointTemplates.of("GET", "/photos/abc/download"))
        assertEquals("GET /users/{username}/photos", EndpointTemplates.of("GET", "/users/poorkane/photos"))
        assertEquals("GET /topics/{id_or_slug}/photos", EndpointTemplates.of("GET", "/topics/nature/photos"))
        assertEquals("DELETE /collections/{id}/remove", EndpointTemplates.of("DELETE", "/collections/206/remove"))
    }

    @Test
    fun fixedSegmentWinsOverPlaceholder() {
        assertEquals("GET /photos/random", EndpointTemplates.of("GET", "/photos/random"))
        assertEquals("GET /search/photos", EndpointTemplates.of("GET", "/search/photos"))
        assertEquals("POST /oauth/token", EndpointTemplates.of("POST", "/oauth/token"))
        assertEquals("GET /me", EndpointTemplates.of("GET", "/me"))
    }

    @Test
    fun unknownPathKeepsOnlyFirstSegment() {
        assertEquals("GET /unknown/*", EndpointTemplates.of("GET", "/unknown/abc/def"))
        assertEquals("GET /unknown", EndpointTemplates.of("GET", "/unknown"))
    }

    @Test
    fun pluginReportsEndpointAndRateLimit() = runBlocking {
        val collector = RequestStatsCollectorImpl()
        val client = client(
            collector = collector,
            status = HttpStatusCode.Forbidden,
            headers = headersOf(
                "X-Ratelimit-Limit" to listOf("50"),
                "X-Ratelimit-Remaining" to listOf("0")
            )
        )

        TopicsApi(client).getTopic("nature")

        val record = collector.records.first()
        assertEquals("GET /topics/{id_or_slug}", record.endpoint)
        assertEquals(50, record.rateLimit)
        assertEquals(0, record.rateLimitRemaining)
        client.close()
    }

    @Test
    fun answerWithoutLimitHeadersHasNoLimit() = runBlocking {
        val collector = RequestStatsCollectorImpl()
        val client = client(collector = collector, status = HttpStatusCode.NotFound, headers = Headers.Empty)

        TopicsApi(client).getTopic("nature")

        val record = collector.records.first()
        assertEquals("GET /topics/{id_or_slug}", record.endpoint)
        assertNull(record.rateLimit)
        client.close()
    }
}

private fun client(
    collector: RequestStatsCollectorImpl,
    status: HttpStatusCode,
    headers: Headers,
): HttpClient = HttpClientFactory(
    networkConfig = StaticNetworkConfig(baseUrl = "https://api.example.com/"),
    headersCollector = HeadersCollectorRepositoryImpl(),
    requestStatsCollector = collector,
    sessionExpiryCollector = SessionExpiryCollectorImpl()
).create(engine = MockEngine { respond(content = "", status = status, headers = headers) })