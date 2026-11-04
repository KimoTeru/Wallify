package org.kimoteru.wallify.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats

/** Both behave like `PhotoRepository.getPhotoFlow`: the database first, refreshed from the backend. */
interface StatsRepository {

    fun getTotalStatsFlow(): Flow<Resource<TotalStats>>

    fun getMonthStatsFlow(): Flow<Resource<MonthStats>>
}