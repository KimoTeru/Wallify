package org.kimoteru.wallify.core.model.stats

import kotlinx.serialization.Serializable

/** Statistics of a photo or a user for the requested number of days. */
@Serializable
data class Statistics(
    val downloads: StatSeries,
    val views: StatSeries
)

/** [total] is the counter for all time, [change] is the sum over the range, [values] hold one point per day. */
@Serializable
data class StatSeries(
    val total: Long,
    val change: Long,
    val values: List<StatPoint>
)

@Serializable
data class StatPoint(
    val date: String,
    val value: Long
)