package org.kimoteru.wallify.core.data.repository

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
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
 * the last [HISTORY_DAYS] days are kept.
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
            stats.value = responseOf { local.read() }.getOrNull() ?: RequestStats.Empty
            loaded.complete(Unit)

            launch {
                collector.records.collect { record ->
                    stats.update { current -> current.record(record) }
                }
            }

            launch {
                stats
                    .mapDistinctBy { current -> current.copy(rateLimit = null) }
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
        stats.update { current -> RequestStats.Empty.copy(rateLimit = current.rateLimit) }
        write(RequestStats.Empty)
    }

    private suspend fun write(current: RequestStats): Response<Unit> = responseOf {
        writeMutex.withLock { local.write(current) }
    }

    private fun RequestStats.record(record: RequestRecord): RequestStats {
        val counted = endpoints.toMutableList()
        val index = counted.indexOfFirst { it.endpoint == record.endpoint }
        if (index >= 0) {
            counted[index] = counted[index].let { it.copy(count = it.count + 1) }
        } else {
            counted += EndpointStat(endpoint = record.endpoint, count = 1L)
        }
        val limit = record.rateLimit
        val remaining = record.rateLimitRemaining
        return copy(
            total = total + 1,
            endpoints = counted.sortedByDescending { it.count },
            rateLimit = if (limit != null && remaining != null) RateLimit(limit, remaining) else rateLimit,
            daily = daily.plusRequest(day = today(), keepDays = HISTORY_DAYS)
        )
    }

    private fun today(): Long {
        val now = time.nowEpochMillis()
        return localEpochDay(epochMillis = now, offsetMillis = timeZone.offsetMillis(now))
    }

    private companion object {
        val SAVE_DELAY = 1.seconds

        /** One day more than the settings screen shows: a change of the time zone may move today by a day. */
        const val HISTORY_DAYS = 31
    }
}