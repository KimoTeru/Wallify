package org.kimoteru.wallify.core.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository
import org.kimoteru.wallify.core.network.session.SessionExpiryCollector

/**
 * The one place that ends a rejected session: the HTTP client reports every 401 to a request sent with the user's
 * token ([SessionExpiryCollector]), whichever screen or background load sent it, and here the session is cleared.
 * `SessionDataCleaner` then wipes the user's data and the root graph shows the sign-in.
 */
class SessionExpiryHandler(
    private val headersCollector: HeadersCollectorRepository,
    private val sessionExpiry: SessionExpiryCollector,
    private val session: SessionRepository,
    private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            sessionExpiry.rejectedAuthorizations.collect { authorization -> endSessionIfCurrent(authorization) }
        }
    }

    /**
     * A request sent with [authorization] was answered with 401. The session ends only while that is still its
     * token: a late answer to a request sent before a new sign-in must not sign the new user out.
     */
    private suspend fun endSessionIfCurrent(authorization: String) {
        if (headersCollector.headersFlow.value.authorization == authorization) session.clear()
    }
}