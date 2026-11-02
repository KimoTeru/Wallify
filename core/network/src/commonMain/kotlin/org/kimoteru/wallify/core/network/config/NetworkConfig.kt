package org.kimoteru.wallify.core.network.config

/**
 * Host, timeouts and static headers for the shared HTTP client.
 * The application supplies every value. This module embeds no secrets.
 */
interface NetworkConfig {
    val baseUrl: String
    val enableLogging: Boolean
    val connectTimeoutMillis: Long
    val requestTimeoutMillis: Long
    val socketTimeoutMillis: Long

    /** Headers added to every request, e.g. `Authorization` and `Accept-Version`. */
    val defaultHeaders: Map<String, String>

    /** Header names whose values are masked in logs. */
    val sensitiveHeaders: Set<String>

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS: Long = 30_000L
    }
}

data class StaticNetworkConfig(
    override val baseUrl: String,
    override val enableLogging: Boolean = false,
    override val connectTimeoutMillis: Long = NetworkConfig.DEFAULT_TIMEOUT_MILLIS,
    override val requestTimeoutMillis: Long = NetworkConfig.DEFAULT_TIMEOUT_MILLIS,
    override val socketTimeoutMillis: Long = NetworkConfig.DEFAULT_TIMEOUT_MILLIS,
    override val defaultHeaders: Map<String, String> = emptyMap(),
    override val sensitiveHeaders: Set<String> = setOf("Authorization")
) : NetworkConfig