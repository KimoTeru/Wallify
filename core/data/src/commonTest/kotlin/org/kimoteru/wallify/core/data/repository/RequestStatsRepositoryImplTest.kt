package org.kimoteru.wallify.core.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.data.cache.FakeAppDispatchers
import org.kimoteru.wallify.core.data.stats.RequestStatsLocalDataSource
import org.kimoteru.wallify.core.model.stats.RateLimit
import org.kimoteru.wallify.core.model.stats.RequestStats
import org.kimoteru.wallify.core.network.stats.RequestRecord
import org.kimoteru.wallify.core.network.stats.RequestStatsCollector
import org.kimoteru.wallify.core.platform.time.TimeProvider
import org.kimoteru.wallify.core.platform.time.TimeZoneProvider

class RequestStatsRepositoryImplTest {

    private val hour = 1.hours.inWholeMilliseconds
    private val collector = FakeCollector()

    @Test
    fun limitOfAnEarlierHourIsAnEstimateOfTheFullOne() = runTest {
        val repository = repository(saved = savedLimit(remaining = 7), nowMillis = hour + 5.minutes.inWholeMilliseconds)

        val rateLimit = repository.stats.value.rateLimit!!

        assertTrue(rateLimit.isEstimated)
        assertEquals(rateLimit.limit, rateLimit.remaining)
    }

    @Test
    fun limitOfTheCurrentHourStaysExact() = runTest {
        val repository = repository(saved = savedLimit(remaining = 7), nowMillis = 5.minutes.inWholeMilliseconds)

        val rateLimit = repository.stats.value.rateLimit!!

        assertFalse(rateLimit.isEstimated)
        assertEquals(7, rateLimit.remaining)
    }

    @Test
    fun headersOfTheNextAnswerReplaceTheEstimate() = runTest {
        val repository = repository(saved = savedLimit(remaining = 7), nowMillis = hour + 5.minutes.inWholeMilliseconds)

        collector.records.tryEmit(RequestRecord(endpoint = "GET /photos", rateLimit = 50, rateLimitRemaining = 41))

        val rateLimit = repository.stats.value.rateLimit!!
        assertFalse(rateLimit.isEstimated)
        assertEquals(41, rateLimit.remaining)
        assertEquals(50, rateLimit.limit)
    }

    @Test
    fun answerWithoutHeadersKeepsTheEstimate() = runTest {
        val repository = repository(saved = savedLimit(remaining = 7), nowMillis = hour + 5.minutes.inWholeMilliseconds)

        collector.records.tryEmit(RequestRecord(endpoint = "GET /photos", rateLimit = null, rateLimitRemaining = null))

        val rateLimit = repository.stats.value.rateLimit!!
        assertTrue(rateLimit.isEstimated)
        assertEquals(rateLimit.limit, rateLimit.remaining)
    }

    private fun savedLimit(remaining: Int) = RequestStats(
        rateLimit = RateLimit(limit = 50, remaining = remaining, receivedAtMillis = 1_000L)
    )

    private fun TestScope.repository(saved: RequestStats, nowMillis: Long) = RequestStatsRepositoryImpl(
        local = FakeLocal(saved),
        collector = collector,
        dispatchers = FakeAppDispatchers(),
        time = object : TimeProvider {
            override fun nowEpochMillis(): Long = nowMillis
        },
        timeZone = object : TimeZoneProvider {
            override fun offsetMillis(epochMillis: Long): Long = 0L
        },
        scope = backgroundScope
    )
}

private class FakeLocal(private var saved: RequestStats) : RequestStatsLocalDataSource {
    override suspend fun read(): RequestStats = saved

    override suspend fun write(stats: RequestStats) {
        saved = stats
    }
}

private class FakeCollector : RequestStatsCollector {
    override val records = MutableSharedFlow<RequestRecord>(extraBufferCapacity = 8)

    override fun onRequest(record: RequestRecord) {
        records.tryEmit(record)
    }
}