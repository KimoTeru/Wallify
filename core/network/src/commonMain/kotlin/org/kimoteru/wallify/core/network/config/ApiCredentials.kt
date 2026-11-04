package org.kimoteru.wallify.core.network.config

/**
 * Keys that identify the application to the backend, both from the build (`ApiBuildConfig`).
 *
 * [accessKey] goes into the static `Authorization` header (see [apiNetworkConfig]).
 * [secretKey] is never sent with ordinary requests: it is only for exchanging a user authorization for a token,
 * so it is injected where that exchange is implemented. [toString] hides both values so they cannot leak into logs.
 */
class ApiCredentials(
    val accessKey: String,
    val secretKey: String
) {
    init {
        require(accessKey.isNotBlank()) { "ApiCredentials.accessKey must not be blank" }
        require(secretKey.isNotBlank()) { "ApiCredentials.secretKey must not be blank" }
    }

    override fun toString(): String = "ApiCredentials(accessKey=***, secretKey=***)"
}