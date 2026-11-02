package org.kimoteru.wallify.core.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.domain.repository.UserSettingsRepository
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository
import org.kimoteru.wallify.core.network.headers.HeadersRequest
import org.kimoteru.wallify.core.platform.systemLanguageTag

/**
 * Keeps [HeadersCollectorRepository] in sync with the session and settings, so the HTTP client
 * always sends the current user token (`Bearer <token>`) and `Accept-Language`.
 * Without a token, the static `Authorization` header from the network config is used.
 */
class SessionHeadersSynchronizer(
    private val session: SessionRepository,
    private val settings: UserSettingsRepository,
    private val headersCollector: HeadersCollectorRepository,
    private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            combine(
                session.authToken,
                settings.observe().map { it.languageTag }
            ) { token, languageTag ->
                HeadersRequest(
                    languageTag = languageTag ?: systemLanguageTag(),
                    authorization = token?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }
                )
            }
                .distinctUntilChanged()
                .collect(headersCollector::setHeaders)
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}