package org.kimoteru.wallify.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.database.entity.MonthStatsEntity
import org.kimoteru.wallify.core.database.entity.StatisticsEntity
import org.kimoteru.wallify.core.database.entity.TotalStatsEntity

@Dao
interface StatsDao {
    @Upsert
    suspend fun upsertStatistics(statistics: StatisticsEntity)

    @Query("SELECT * FROM statistics WHERE `key` = :key")
    fun observeStatistics(key: String): Flow<StatisticsEntity?>

    @Upsert
    suspend fun upsertTotal(stats: TotalStatsEntity)

    @Query("SELECT * FROM total_stats LIMIT 1")
    fun observeTotal(): Flow<TotalStatsEntity?>

    @Upsert
    suspend fun upsertMonth(stats: MonthStatsEntity)

    @Query("SELECT * FROM month_stats LIMIT 1")
    fun observeMonth(): Flow<MonthStatsEntity?>

    /** Drops the statistics whose freshness (`item:` + key in `list_state`) was pruned. */
    @Query("DELETE FROM statistics WHERE 'item:' || `key` NOT IN (SELECT listKey FROM list_state)")
    suspend fun deleteStatisticsWithoutState()

    @Query("DELETE FROM statistics")
    suspend fun deleteAllStatistics()

    @Query("DELETE FROM total_stats")
    suspend fun deleteTotal()

    @Query("DELETE FROM month_stats")
    suspend fun deleteMonth()
}