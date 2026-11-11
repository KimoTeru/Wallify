package org.kimoteru.wallify.core.network.session

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Singleton

/**
 * Conflated queue: sending never suspends the request, and several requests rejected at once end the session once.
 * A rejection sent before anyone reads it is kept.
 */
@Singleton
class SessionExpiryCollectorImpl : SessionExpiryCollector {
    private val queue = Channel<String>(Channel.CONFLATED)

    override val rejectedAuthorizations: Flow<String> = queue.receiveAsFlow()

    override fun onRejected(authorization: String) {
        queue.trySend(authorization)
    }
}