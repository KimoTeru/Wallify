package org.kimoteru.wallify.core.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.stats.DailyRequests
import org.kimoteru.wallify.core.model.stats.RequestStats

/**
 * Counter of the requests to the backend. It is kept on the device across restarts; the end of the session wipes it
 * together with the other data of the user.
 */
interface RequestStatsRepository {
    val stats: StateFlow<RequestStats>

    /**
     * Requests of [stats] per local day of the user for exactly [days] days that end today, the oldest first; days
     * without requests are zero. Today is taken at the call, so a new day is seen without a new request.
     */
    fun lastDays(stats: RequestStats, days: Int): List<DailyRequests>

    /** Sets every counter and the days to zero; the last known rate limit stays, it belongs to the backend. */
    suspend fun reset(): Response<Unit>
}