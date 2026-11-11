package org.kimoteru.wallify.core.data.stats

import org.kimoteru.wallify.core.model.stats.DailyRequests
import kotlin.test.Test
import kotlin.test.assertEquals

class DailyRequestsHistoryTest {

    @Test
    fun `local day follows the offset of the time zone`() {
        val lateEveningUtc = DAY_MILLIS * 100 + DAY_MILLIS - HOUR_MILLIS

        assertEquals(100L, localEpochDay(lateEveningUtc, offsetMillis = 0L))
        assertEquals(101L, localEpochDay(lateEveningUtc, offsetMillis = 3 * HOUR_MILLIS))
        assertEquals(99L, localEpochDay(DAY_MILLIS * 100, offsetMillis = -HOUR_MILLIS))
    }

    @Test
    fun `request is added to its day or starts a new one`() {
        val history = listOf(DailyRequests(day = 10L, count = 2L))

        assertEquals(listOf(DailyRequests(day = 10L, count = 3L)), history.plusRequest(day = 10L, keepDays = 31))
        assertEquals(
            listOf(DailyRequests(day = 10L, count = 2L), DailyRequests(day = 11L, count = 1L)),
            history.plusRequest(day = 11L, keepDays = 31)
        )
    }

    @Test
    fun `days older than the kept period are dropped`() {
        val history = (1L..40L).map { day -> DailyRequests(day = day, count = 1L) }

        val result = history.plusRequest(day = 40L, keepDays = 31)

        assertEquals(31, result.size)
        assertEquals(10L, result.first().day)
        assertEquals(DailyRequests(day = 40L, count = 2L), result.last())
    }

    @Test
    fun `last days are filled with zeros and end today`() {
        val history = listOf(
            DailyRequests(day = 5L, count = 7L),
            DailyRequests(day = 18L, count = 3L),
            DailyRequests(day = 20L, count = 1L)
        )

        val result = history.lastDays(today = 20L, days = 5)

        assertEquals((16L..20L).toList(), result.map { it.day })
        assertEquals(listOf(0L, 0L, 3L, 0L, 1L), result.map { it.count })
    }

    @Test
    fun `empty history gives a row of zeros`() {
        val result = emptyList<DailyRequests>().lastDays(today = 100L, days = 30)

        assertEquals(30, result.size)
        assertEquals(100L, result.last().day)
        assertEquals(0L, result.sumOf { it.count })
    }

    private companion object {
        const val HOUR_MILLIS = 60L * 60L * 1000L
        const val DAY_MILLIS = 24L * HOUR_MILLIS
    }
}