package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.kimoteru.wallify.core.network.api.StatsApi
import org.kimoteru.wallify.core.network.model.response.stats.toDomain
import org.koin.core.annotation.Singleton

@Singleton
class StatsRemoteDataSourceImpl(
    private val statsApi: StatsApi
) : StatsRemoteDataSource {

    override suspend fun getTotalStats(): Response<TotalStats> {
        return statsApi.getTotalStats()
            .map { it.toDomain() }
    }

    override suspend fun getMonthStats(): Response<MonthStats> {
        return statsApi.getMonthStats()
            .map { it.toDomain() }
    }
}