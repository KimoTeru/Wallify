package org.kimoteru.wallify.core.data.stats

import kotlin.time.Duration.Companion.days
import org.kimoteru.wallify.core.model.stats.DailyRequests

private val MILLIS_PER_DAY = 1.days.inWholeMilliseconds

/** Local day of [epochMillis] in days since 1970-01-01; [offsetMillis] is the offset of the local time from UTC. */
internal fun localEpochDay(epochMillis: Long, offsetMillis: Long): Long =
    (epochMillis + offsetMillis).floorDiv(MILLIS_PER_DAY)

/**
 * Adds one request to [day] and drops the days that are [keepDays] or more before it, so the saved history stays
 * short. The result is sorted by day, the oldest first.
 */
internal fun List<DailyRequests>.plusRequest(day: Long, keepDays: Int): List<DailyRequests> {
    val counted = filter { it.day > day - keepDays }.toMutableList()
    val index = counted.indexOfFirst { it.day == day }
    if (index >= 0) {
        counted[index] = counted[index].let { it.copy(count = it.count + 1) }
    } else {
        counted += DailyRequests(day = day, count = 1L)
    }
    return counted.sortedBy { it.day }
}

/** Exactly [days] days that end with [today], the oldest first; a day without requests has a zero count. */
internal fun List<DailyRequests>.lastDays(today: Long, days: Int): List<DailyRequests> {
    val counts = associate { it.day to it.count }
    return (today - days + 1..today).map { day -> DailyRequests(day = day, count = counts[day] ?: 0L) }
}