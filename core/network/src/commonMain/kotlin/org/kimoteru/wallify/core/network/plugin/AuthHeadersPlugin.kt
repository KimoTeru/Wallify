package org.kimoteru.wallify.core.network.plugin

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.AuthScheme
import org.kimoteru.wallify.core.common.extensions.nullIfBlank
import org.kimoteru.wallify.core.network.config.OAUTH_BASE_URL
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository
import org.kimoteru.wallify.core.network.session.SessionExpiryCollector

internal class AuthHeadersConfig {
    lateinit var headersCollector: HeadersCollectorRepository
    lateinit var sessionExpiryCollector: SessionExpiryCollector
}

/**
 * Adds session-dependent headers to every request and notices when the backend stops accepting the session.
 * Sets `Authorization` when the session has a user token (replacing the static header from the config)
 * and `Accept-Language` when a language is known. A 401 to a request that went with the user's token (`Bearer`)
 * goes to [AuthHeadersConfig.sessionExpiryCollector]; a 401 to the anonymous client or to the OAuth pages (the
 * exchange of the sign-in code is answered with 401 when the app itself is rejected) is not an expired session.
 */
internal val AuthHeadersPlugin = createClientPlugin("AuthHeaders", ::AuthHeadersConfig) {
    val collector = pluginConfig.headersCollector
    val sessionExpiryCollector = pluginConfig.sessionExpiryCollector

    onRequest { request, _ ->
        val headers = collector.headersFlow.value
        headers.authorization
            ?.nullIfBlank()
            ?.let { request.headers[HttpHeaders.Authorization] = it }
        headers.languageTag
            ?.nullIfBlank()
            ?.let { request.headers[HttpHeaders.AcceptLanguage] = it }
    }

    onResponse { response ->
        if (response.status != HttpStatusCode.Unauthorized) return@onResponse

        val request = response.call.request
        if (request.url.toString().startsWith(OAUTH_BASE_URL)) return@onResponse

        request.headers[HttpHeaders.Authorization]
            ?.takeIf { it.startsWith(AuthScheme.Bearer, ignoreCase = true) }
            ?.let(sessionExpiryCollector::onRejected)
    }
}