package org.kimoteru.wallify.core.data.repository

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.data.remote.StatsRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.StatsRepository
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.koin.core.annotation.Singleton

@Singleton
class StatsRepositoryImpl(
    private val statsRemoteDataSource: StatsRemoteDataSource
) : StatsRepository {

    override suspend fun getTotalStats(): Response<TotalStats> {
        return statsRemoteDataSource.getTotalStats()
    }

    override suspend fun getMonthStats(): Response<MonthStats> {
        return statsRemoteDataSource.getMonthStats()
    }
}