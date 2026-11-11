package org.kimoteru.wallify.core.network.stats

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Singleton

/** Unlimited queue: sending never suspends the request, and records sent before anyone reads them are kept. */
@Singleton
class RequestStatsCollectorImpl : RequestStatsCollector {
    private val queue = Channel<RequestRecord>(Channel.UNLIMITED)

    override val records: Flow<RequestRecord> = queue.receiveAsFlow()

    override fun onRequest(record: RequestRecord) {
        queue.trySend(record)
    }
}