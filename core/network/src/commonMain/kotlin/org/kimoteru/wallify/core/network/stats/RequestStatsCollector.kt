package org.kimoteru.wallify.core.network.stats

import kotlinx.coroutines.flow.Flow

/**
 * Every answered request of the API client. The client calls [onRequest] on the thread of the request, so it only
 * queues the record; the data layer reads [records] and counts them. Records wait in the queue until they are read.
 */
interface RequestStatsCollector {
    val records: Flow<RequestRecord>

    fun onRequest(record: RequestRecord)
}

/**
 * One answered request. [endpoint] is the method and the path template (`GET /photos/{id}`); [rateLimit] and
 * [rateLimitRemaining] are `null` when the answer carries no rate limit headers.
 */
data class RequestRecord(
    val endpoint: String,
    val rateLimit: Int?,
    val rateLimitRemaining: Int?,
)