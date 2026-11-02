package org.kimoteru.wallify.core.network.plugin

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository

internal class AuthHeadersConfig {
    lateinit var headersCollector: HeadersCollectorRepository
}

/**
 * Adds session-dependent headers to every request.
 * Sets `Authorization` when the session has a user token (replacing the static header from the config)
 * and `Accept-Language` when a language is known.
 */
internal val AuthHeadersPlugin = createClientPlugin("AuthHeaders", ::AuthHeadersConfig) {
    val collector = pluginConfig.headersCollector

    onRequest { request, _ ->
        val headers = collector.headersFlow.value
        headers.authorization
            ?.takeIf { it.isNotBlank() }
            ?.let { request.headers[HttpHeaders.Authorization] = it }
        headers.languageTag
            ?.takeIf { it.isNotBlank() }
            ?.let { request.headers[HttpHeaders.AcceptLanguage] = it }
    }
}