package org.kimoteru.wallify.core.model.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Requests the app sent to the backend on this device since the last reset.
 * [endpoints] are sorted by [EndpointStat.count], the most requested first. [rateLimit] comes from the last answer of
 * the backend and is `null` until the first answer after the start of the app: the limit resets every hour.
 * [daily] holds only the recent local days of the user that had requests, the oldest first; the defaults let the
 * counters saved before a field appeared be read.
 */
@Serializable
data class RequestStats(
    @SerialName("total")
    val total: Long = 0L,

    @SerialName("endpoints")
    val endpoints: List<EndpointStat> = emptyList(),

    @SerialName("rate_limit")
    val rateLimit: RateLimit? = null,

    @SerialName("daily")
    val daily: List<DailyRequests> = emptyList(),
) {
    companion object {
        val Empty: RequestStats = RequestStats()
    }
}

/** [day] is the local day of the user counted in days since 1970-01-01; [count] is the requests sent that day. */
@Serializable
data class DailyRequests(
    @SerialName("day")
    val day: Long,

    @SerialName("count")
    val count: Long,
)

/** [endpoint] is the method and the path template, e.g. `GET /photos/{id}`. */
@Serializable
data class EndpointStat(
    @SerialName("endpoint")
    val endpoint: String,

    @SerialName("count")
    val count: Long,
)

/** Requests allowed per hour ([limit]) and how many of them are left ([remaining]). */
@Serializable
data class RateLimit(
    @SerialName("limit")
    val limit: Int,

    @SerialName("remaining")
    val remaining: Int,
)