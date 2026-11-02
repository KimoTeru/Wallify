package org.kimoteru.wallify.core.network.config

/**
 * Builds the [NetworkConfig] for the backend API.
 *
 * Authentication and versioning are static headers, defined here and nowhere else.
 * Change them in this function if the backend uses another scheme.
 * [accessKey] must come from the build or the environment, never from VCS.
 */
fun apiNetworkConfig(
    baseUrl: String,
    accessKey: String,
    enableLogging: Boolean = false
): NetworkConfig = StaticNetworkConfig(
    baseUrl = baseUrl,
    enableLogging = enableLogging,
    defaultHeaders = buildMap {
        put(ACCEPT_VERSION_HEADER, API_VERSION)
        if (accessKey.isNotBlank()) {
            put(AUTHORIZATION_HEADER, "$ACCESS_KEY_SCHEME $accessKey")
        }
    }
)

private const val ACCEPT_VERSION_HEADER = "Accept-Version"
private const val AUTHORIZATION_HEADER = "Authorization"
private const val API_VERSION = "v1"
private const val ACCESS_KEY_SCHEME = "Client-ID"