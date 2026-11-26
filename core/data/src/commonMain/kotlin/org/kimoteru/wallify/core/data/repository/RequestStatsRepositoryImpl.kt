package org.kimoteru.wallify.core.data.repository

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.getOrNull
import org.kimoteru.wallify.core.common.response.responseOf
import org.kimoteru.wallify.core.data.stats.RequestStatsLocalDataSource
import org.kimoteru.wallify.core.data.stats.lastDays
import org.kimoteru.wallify.core.data.stats.localEpochDay
import org.kimoteru.wallify.core.data.stats.plusRequest
import org.kimoteru.wallify.core.domain.repository.RequestStatsRepository
import org.kimoteru.wallify.core.model.stats.DailyRequests
import org.kimoteru.wallify.core.model.stats.EndpointStat
import org.kimoteru.wallify.core.model.stats.RateLimit
import org.kimoteru.wallify.core.model.stats.RequestStats
import org.kimoteru.wallify.core.network.stats.RequestRecord
import org.kimoteru.wallify.core.network.stats.RequestStatsCollector
import org.kimoteru.wallify.core.platform.time.TimeProvider
import org.kimoteru.wallify.core.platform.time.TimeZoneProvider
import org.koin.core.annotation.Singleton

/**
 * One [StateFlow] is the truth. The saved counters are read first, then the records of the HTTP client are added one
 * by one (they wait in the collector's queue meanwhile, none is lost), and a moment after the last change the
 * counters are saved, so a burst of requests is one write. Created at start: otherwise nobody reads the queue until
 * the settings screen opens. Every request is also counted to the local day of the user it was answered on; only
 * the last [HISTORY_DAYS] days are kept. The rate limit is reset to the full one at the start of every hour, like the
 * backend does, so [stats] is current without a request.
 */
@Singleton(createdAtStart = true)
class RequestStatsRepositoryImpl(
    private val local: RequestStatsLocalDataSource,
    private val collector: RequestStatsCollector,
    private val dispatchers: AppDispatchers,
    private val time: TimeProvider,
    private val timeZone: TimeZoneProvider,
    scope: CoroutineScope,
) : RequestStatsRepository {

    private val loaded = CompletableDeferred<Unit>()
    private val writeMutex = Mutex()

    override val stats: StateFlow<RequestStats>
        field = MutableStateFlow(RequestStats.Empty)

    init {
        scope.launch(dispatchers.io) {
            val saved = responseOf { local.read() }.getOrNull() ?: RequestStats.Empty
            stats.value = saved.withHourReset()
            loaded.complete(Unit)

            launch {
                while (true) {
                    val windowMillis = RATE_LIMIT_WINDOW.inWholeMilliseconds
                    delay((windowMillis - time.nowEpochMillis() % windowMillis).milliseconds)
                    stats.update { current -> current.withHourReset() }
                }
            }

            launch {
                collector.records.collect { record ->
                    stats.update { current -> current.record(record) }
                }
            }

            launch {
                stats
                    .collectLatest { current ->
                        delay(SAVE_DELAY)
                        write(current)
                    }
            }
        }
    }

    override fun lastDays(stats: RequestStats, days: Int): List<DailyRequests> =
        stats.daily.lastDays(today = today(), days = days)

    override suspend fun reset(): Response<Unit> = withContext(dispatchers.io) {
        loaded.await()
        val cleared = stats.updateAndGet { current -> RequestStats.Empty.copy(rateLimit = current.rateLimit) }
        write(cleared)
    }

    // The backend does not document its window, so the full limit after the hour changed is a guess: it is marked
    // as estimated until an answer brings the real headers.
    private fun RequestStats.withHourReset(): RequestStats {
        val known = rateLimit ?: return this
        val hourStart = time.nowEpochMillis().let { now -> now - now % RATE_LIMIT_WINDOW.inWholeMilliseconds }
        if (known.receivedAtMillis >= hourStart) return this

        return copy(
            rateLimit = known.copy(
                remaining = known.limit,
                receivedAtMillis = hourStart,
                isEstimated = true
            )
        )
    }

    private suspend fun write(current: RequestStats): Response<Unit> = responseOf {
        writeMutex.withLock { local.write(current) }
    }

    private fun RequestStats.record(record: RequestRecord): RequestStats {
        val counted = endpoints.toMutableList()
        val index = counted.indexOfFirst { it.endpoint == record.endpoint }
        if (index >= 0) {
            counted[index] = counted[index].let { it.copy(count = it.count.inc()) }
        } else {
            counted += EndpointStat(endpoint = record.endpoint, count = 1L)
        }
        val limit = record.rateLimit
        val remaining = record.rateLimitRemaining
        return copy(
            total = total.inc(),
            endpoints = counted.sortedByDescending { it.count },
            rateLimit = if (limit != null && remaining != null) {
                RateLimit(limit = limit, remaining = remaining, receivedAtMillis = time.nowEpochMillis())
            } else {
                rateLimit
            },
            daily = daily.plusRequest(day = today(), keepDays = HISTORY_DAYS)
        )
    }

    private fun today(): Long {
        val now = time.nowEpochMillis()
        return localEpochDay(epochMillis = now, offsetMillis = timeZone.offsetMillis(now))
    }

    private companion object {
        val SAVE_DELAY = 1.seconds

        /** The backend gives the limit per clock hour and resets it at its start. */
        val RATE_LIMIT_WINDOW = 1.hours

        /** One day more than the settings screen shows: a change of the time zone may move today by a day. */
        const val HISTORY_DAYS = 31
    }
}