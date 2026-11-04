package org.kimoteru.wallify.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.data.cache.CacheStore
import org.kimoteru.wallify.core.data.cache.refreshableFlow
import org.kimoteru.wallify.core.data.remote.StatsRemoteDataSource
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.entity.toDomain
import org.kimoteru.wallify.core.domain.repository.StatsRepository
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats
import org.koin.core.annotation.Singleton

@Singleton
class StatsRepositoryImpl(
    private val statsRemoteDataSource: StatsRemoteDataSource,
    private val statsDao: StatsDao,
    private val cacheStore: CacheStore
) : StatsRepository {

    override fun getTotalStatsFlow(): Flow<Resource<TotalStats>> {
        return refreshableFlow(
            roomFlow = { statsDao.observeTotal() },
            remoteCall = { statsRemoteDataSource.getTotalStats() },
            save = cacheStore::saveTotalStats,
            toDomain = { entity -> entity.toDomain() }
        )
    }

    override fun getMonthStatsFlow(): Flow<Resource<MonthStats>> {
        return refreshableFlow(
            roomFlow = { statsDao.observeMonth() },
            remoteCall = { statsRemoteDataSource.getMonthStats() },
            save = cacheStore::saveMonthStats,
            toDomain = { entity -> entity.toDomain() }
        )
    }
}