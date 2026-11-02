package org.kimoteru.wallify.core.network.headers

/**
 * Dynamic values attached to every request.
 *
 * [authorization] is sent as-is (include the scheme, e.g. "Bearer <token>") and replaces the static
 * `Authorization` header from the network config. When it is null, the static header is kept.
 */
data class HeadersRequest(
    val languageTag: String? = null,
    val authorization: String? = null
)