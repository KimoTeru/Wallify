package org.kimoteru.wallify.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.network.model.response.stats.MonthStatsResponse
import org.kimoteru.wallify.core.network.model.response.stats.TotalStatsResponse
import org.kimoteru.wallify.core.network.response.executeRequest
import org.koin.core.annotation.Singleton

/** Endpoints under `stats`: counters for the whole service. */
@Singleton
class StatsApi(
    private val client: HttpClient
) {

    suspend fun getTotalStats(): Response<TotalStatsResponse> {
        return executeRequest {
            client.get("stats/total")
        }
    }

    suspend fun getMonthStats(): Response<MonthStatsResponse> {
        return executeRequest {
            client.get("stats/month")
        }
    }
}