package org.kimoteru.wallify.core.network.plugin

import io.ktor.client.plugins.api.createClientPlugin
import org.kimoteru.wallify.core.network.api.EndpointTemplates
import org.kimoteru.wallify.core.network.stats.RequestRecord
import org.kimoteru.wallify.core.network.stats.RequestStatsCollector

internal class RequestStatsConfig {
    lateinit var collector: RequestStatsCollector
}

/**
 * Reports every request the backend answered (any status) to [RequestStatsConfig.collector]: the endpoint template
 * and the rate limit from the answer headers. Requests that never reached the backend (no network) are not counted.
 */
internal val RequestStatsPlugin = createClientPlugin("RequestStats", ::RequestStatsConfig) {
    val collector = pluginConfig.collector

    onResponse { response ->
        val request = response.call.request
        collector.onRequest(
            RequestRecord(
                endpoint = EndpointTemplates.of(request.method.value, request.url.encodedPath),
                rateLimit = response.headers[RATE_LIMIT_HEADER]?.toIntOrNull(),
                rateLimitRemaining = response.headers[RATE_LIMIT_REMAINING_HEADER]?.toIntOrNull()
            )
        )
    }
}

/** Rate limit headers of the backend: requests per hour and how many of them are left. */
private const val RATE_LIMIT_HEADER = "X-Ratelimit-Limit"
private const val RATE_LIMIT_REMAINING_HEADER = "X-Ratelimit-Remaining"