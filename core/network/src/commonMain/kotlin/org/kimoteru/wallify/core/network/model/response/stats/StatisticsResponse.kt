package org.kimoteru.wallify.core.network.model.response.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.stats.StatPoint
import org.kimoteru.wallify.core.model.stats.StatSeries
import org.kimoteru.wallify.core.model.stats.Statistics

@Serializable
data class PhotoStatisticsResponse(
    @SerialName("id")
    val id: String? = null,

    @SerialName("downloads")
    val downloads: StatSeriesResponse? = null,

    @SerialName("views")
    val views: StatSeriesResponse? = null
)

@Serializable
data class UserStatisticsResponse(
    @SerialName("username")
    val username: String? = null,

    @SerialName("downloads")
    val downloads: StatSeriesResponse? = null,

    @SerialName("views")
    val views: StatSeriesResponse? = null
)

@Serializable
data class StatSeriesResponse(
    @SerialName("total")
    val total: Long? = null,

    @SerialName("historical")
    val historical: HistoricalStatResponse? = null
)

@Serializable
data class HistoricalStatResponse(
    @SerialName("change")
    val change: Long? = null,

    @SerialName("average")
    val average: Long? = null,

    @SerialName("resolution")
    val resolution: String? = null,

    @SerialName("quantity")
    val quantity: Int? = null,

    @SerialName("values")
    val values: List<StatValueResponse> = emptyList()
)

@Serializable
data class StatValueResponse(
    @SerialName("date")
    val date: String,

    @SerialName("value")
    val value: Long
)

fun PhotoStatisticsResponse.toDomain(): Statistics = Statistics(
    downloads = downloads.toDomain(),
    views = views.toDomain()
)

fun UserStatisticsResponse.toDomain(): Statistics = Statistics(
    downloads = downloads.toDomain(),
    views = views.toDomain()
)

/** A missing series is an empty one: the backend omits counters it has nothing to report for. */
fun StatSeriesResponse?.toDomain(): StatSeries = StatSeries(
    total = this?.total ?: 0,
    change = this?.historical?.change ?: 0,
    values = this?.historical?.values.orEmpty().map { value -> value.toDomain() }
)

fun StatValueResponse.toDomain(): StatPoint = StatPoint(
    date = date,
    value = value
)