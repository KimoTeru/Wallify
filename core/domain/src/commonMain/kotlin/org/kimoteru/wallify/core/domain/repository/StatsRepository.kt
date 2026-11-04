package org.kimoteru.wallify.core.domain.repository

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats

interface StatsRepository {

    suspend fun getTotalStats(): Response<TotalStats>

    suspend fun getMonthStats(): Response<MonthStats>
}