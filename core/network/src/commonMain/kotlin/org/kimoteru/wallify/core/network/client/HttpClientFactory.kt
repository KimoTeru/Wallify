package org.kimoteru.wallify.core.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import org.kimoteru.wallify.core.common.extensions.Slash
import org.kimoteru.wallify.core.network.config.NetworkConfig
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository
import org.kimoteru.wallify.core.network.json.networkJson
import org.kimoteru.wallify.core.network.plugin.AuthHeadersPlugin
import org.koin.core.annotation.Singleton

@Singleton
class HttpClientFactory(
    private val networkConfig: NetworkConfig,
    private val headersCollector: HeadersCollectorRepository
) {
    init {
        require(networkConfig.baseUrl.isNotBlank()) {
            "NetworkConfig.baseUrl must not be blank"
        }
    }

    /**
     * Client for downloading arbitrary files (images, CDN assets).
     * It sends no base URL, API headers or session headers, so credentials never reach third-party hosts.
     */
    fun createPlain(
        engine: HttpClientEngine = platformHttpClientEngine()
    ): HttpClient = HttpClient(engine) {
        expectSuccess = false
        followRedirects = true

        install(HttpTimeout) {
            connectTimeoutMillis = networkConfig.connectTimeoutMillis
            requestTimeoutMillis = DOWNLOAD_REQUEST_TIMEOUT_MILLIS
            socketTimeoutMillis = networkConfig.socketTimeoutMillis
        }
    }

    fun create(
        engine: HttpClientEngine = platformHttpClientEngine()
    ): HttpClient = HttpClient(engine) {
        expectSuccess = false

        install(HttpTimeout) {
            connectTimeoutMillis = networkConfig.connectTimeoutMillis
            requestTimeoutMillis = networkConfig.requestTimeoutMillis
            socketTimeoutMillis = networkConfig.socketTimeoutMillis
        }

        install(ContentNegotiation) {
            json(networkJson)
        }

        if (networkConfig.enableLogging) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
                sanitizeHeader { header ->
                    networkConfig.sensitiveHeaders.any { it.equals(header, ignoreCase = true) }
                }
            }
        }

        install(AuthHeadersPlugin) {
            headersCollector = this@HttpClientFactory.headersCollector
        }

        defaultRequest {
            url(networkConfig.normalizedBaseUrl())
            accept(ContentType.Application.Json)
            networkConfig.defaultHeaders.forEach { (name, value) -> header(name, value) }
        }
    }
}

private const val DOWNLOAD_REQUEST_TIMEOUT_MILLIS = 120_000L

private fun NetworkConfig.normalizedBaseUrl(): String {
    val trimmed = baseUrl.trim()
    return if (trimmed.endsWith(String.Slash)) trimmed else trimmed + String.Slash
}