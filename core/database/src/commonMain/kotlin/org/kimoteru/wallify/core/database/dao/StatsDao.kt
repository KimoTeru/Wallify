package org.kimoteru.wallify.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.database.entity.StatisticsEntity

@Dao
interface StatsDao {
    @Upsert
    suspend fun upsertStatistics(statistics: StatisticsEntity)

    @Query("SELECT * FROM statistics WHERE `key` = :key")
    fun observeStatistics(key: String): Flow<StatisticsEntity?>

    /** Drops the statistics whose freshness (`item:` + key in `list_state`) was pruned. */
    @Query("DELETE FROM statistics WHERE 'item:' || `key` NOT IN (SELECT listKey FROM list_state)")
    suspend fun deleteStatisticsWithoutState()

    @Query("DELETE FROM statistics")
    suspend fun deleteAllStatistics()
}