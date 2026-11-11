package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.kimoteru.wallify.core.model.stats.StatSeries
import org.kimoteru.wallify.core.model.stats.Statistics

/** Statistics of one photo or user for one range of days; [key] says which (for example `photo:<id>:30`). */
@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey val key: String,
    val downloads: StatSeries,
    val views: StatSeries
)

fun Statistics.toEntity(key: String): StatisticsEntity = StatisticsEntity(
    key = key,
    downloads = downloads,
    views = views
)

fun StatisticsEntity.toDomain(): Statistics = Statistics(
    downloads = downloads,
    views = views
)