package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats

interface StatsRemoteDataSource {

    suspend fun getTotalStats(): Response<TotalStats>

    suspend fun getMonthStats(): Response<MonthStats>
}