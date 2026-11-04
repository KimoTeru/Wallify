package org.kimoteru.wallify.core.network.config

/**
 * Builds the [NetworkConfig] for the Unsplash API.
 *
 * The address, authentication and versioning are defined here and nowhere else.
 * Change them in this function if the backend uses another scheme.
 * The access key of [credentials] must come from the build or the environment, never from VCS.
 */
fun apiNetworkConfig(
    credentials: ApiCredentials,
    enableLogging: Boolean = false
): NetworkConfig = StaticNetworkConfig(
    baseUrl = API_BASE_URL,
    enableLogging = enableLogging,
    defaultHeaders = mapOf(
        ACCEPT_VERSION_HEADER to API_VERSION,
        AUTHORIZATION_HEADER to "$ACCESS_KEY_SCHEME ${credentials.accessKey}"
    )
)

private const val API_BASE_URL = "https://api.unsplash.com/"

/** Address of the backend's website; the pages the app opens in the browser and the authorization live there. */
internal const val WEB_BASE_URL = "https://unsplash.com/"

/** Address of the user authorization endpoints; they live on another host than the API. */
internal const val OAUTH_BASE_URL = WEB_BASE_URL + "oauth/"
private const val ACCEPT_VERSION_HEADER = "Accept-Version"
private const val AUTHORIZATION_HEADER = "Authorization"
private const val API_VERSION = "v1"
private const val ACCESS_KEY_SCHEME = "Client-ID"