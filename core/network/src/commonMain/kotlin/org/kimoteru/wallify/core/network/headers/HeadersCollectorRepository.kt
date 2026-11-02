package org.kimoteru.wallify.core.network.headers

import kotlinx.coroutines.flow.StateFlow

/** Holds the current dynamic headers. The HTTP client reads it on every request. */
interface HeadersCollectorRepository {
    val headersFlow: StateFlow<HeadersRequest>

    fun setHeaders(headers: HeadersRequest)

    fun updateAuthorization(authorization: String?)

    fun updateLanguageTag(languageTag: String?)
}