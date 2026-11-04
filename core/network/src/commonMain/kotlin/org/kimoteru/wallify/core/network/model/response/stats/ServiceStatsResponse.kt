package org.kimoteru.wallify.core.network.model.response.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.stats.MonthStats
import org.kimoteru.wallify.core.model.stats.TotalStats

/** Counters for the whole service (`stats/total`). */
@Serializable
data class TotalStatsResponse(
    @SerialName("photos")
    val photos: Long? = null,

    @SerialName("downloads")
    val downloads: Long? = null,

    @SerialName("views")
    val views: Long? = null,

    @SerialName("photographers")
    val photographers: Long? = null,

    @SerialName("pixels")
    val pixels: Long? = null,

    @SerialName("downloads_per_second")
    val downloadsPerSecond: Double? = null,

    @SerialName("views_per_second")
    val viewsPerSecond: Double? = null,

    @SerialName("developers")
    val developers: Long? = null,

    @SerialName("applications")
    val applications: Long? = null,

    @SerialName("requests")
    val requests: Long? = null
)

/** Counters for the last 30 days (`stats/month`). */
@Serializable
data class MonthStatsResponse(
    @SerialName("downloads")
    val downloads: Long? = null,

    @SerialName("views")
    val views: Long? = null,

    @SerialName("new_photos")
    val newPhotos: Long? = null,

    @SerialName("new_photographers")
    val newPhotographers: Long? = null,

    @SerialName("new_pixels")
    val newPixels: Long? = null,

    @SerialName("new_developers")
    val newDevelopers: Long? = null,

    @SerialName("new_applications")
    val newApplications: Long? = null,

    @SerialName("new_requests")
    val newRequests: Long? = null
)

fun TotalStatsResponse.toDomain(): TotalStats = TotalStats(
    photos = photos ?: 0,
    downloads = downloads ?: 0,
    views = views ?: 0,
    photographers = photographers ?: 0
)

fun MonthStatsResponse.toDomain(): MonthStats = MonthStats(
    downloads = downloads ?: 0,
    views = views ?: 0,
    newPhotos = newPhotos ?: 0,
    newPhotographers = newPhotographers ?: 0
)