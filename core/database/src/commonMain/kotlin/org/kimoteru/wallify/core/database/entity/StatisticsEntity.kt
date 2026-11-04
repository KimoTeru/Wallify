package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.StatSeries
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.stats.TotalStats

/** Statistics of one photo or user for one range of days; [key] says which (for example `photo:<id>:30`). */
@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey val key: String,
    val downloads: StatSeries,
    val views: StatSeries
)

/** Counters of the whole service for all time. There is one row. */
@Entity(tableName = "total_stats")
data class TotalStatsEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val photos: Long,
    val downloads: Long,
    val views: Long,
    val photographers: Long
)

/** Counters of the whole service for the last 30 days. There is one row. */
@Entity(tableName = "month_stats")
data class MonthStatsEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val downloads: Long,
    val views: Long,
    val newPhotos: Long,
    val newPhotographers: Long
)

const val SINGLE_ROW_ID = 0

fun Statistics.toEntity(key: String): StatisticsEntity = StatisticsEntity(
    key = key,
    downloads = downloads,
    views = views
)

fun StatisticsEntity.toDomain(): Statistics = Statistics(
    downloads = downloads,
    views = views
)

fun TotalStats.toEntity(): TotalStatsEntity = TotalStatsEntity(
    photos = photos,
    downloads = downloads,
    views = views,
    photographers = photographers
)

fun TotalStatsEntity.toDomain(): TotalStats = TotalStats(
    photos = photos,
    downloads = downloads,
    views = views,
    photographers = photographers
)

fun MonthStats.toEntity(): MonthStatsEntity = MonthStatsEntity(
    downloads = downloads,
    views = views,
    newPhotos = newPhotos,
    newPhotographers = newPhotographers
)

fun MonthStatsEntity.toDomain(): MonthStats = MonthStats(
    downloads = downloads,
    views = views,
    newPhotos = newPhotos,
    newPhotographers = newPhotographers
)