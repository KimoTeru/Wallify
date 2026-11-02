package org.kimoteru.wallify.core.network

import io.ktor.client.HttpClient
import org.kimoteru.wallify.core.network.client.DownloadHttpClient
import org.kimoteru.wallify.core.network.client.HttpClientFactory
import org.kimoteru.wallify.core.network.config.ApiBuildConfig
import org.kimoteru.wallify.core.network.config.NetworkConfig
import org.kimoteru.wallify.core.network.config.apiNetworkConfig
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * Shared HTTP stack.
 *
 * The backend address and key are build constants (`ApiBuildConfig`, generated from `local.properties` or the
 * environment), do not hardcode hosts or keys here. The session layer updates `HeadersCollectorRepository`
 * (user token, language) and the client reads it per request.
 */
@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.core.network")
class NetworkModule {
    @Singleton
    fun networkConfig(): NetworkConfig = apiNetworkConfig(
        baseUrl = ApiBuildConfig.BASE_URL,
        accessKey = ApiBuildConfig.ACCESS_KEY,
        enableLogging = ApiBuildConfig.ENABLE_LOGGING
    )

    @Singleton
    fun httpClient(factory: HttpClientFactory): HttpClient = factory.create()

    @Singleton
    fun downloadHttpClient(factory: HttpClientFactory): DownloadHttpClient =
        DownloadHttpClient(factory.createPlain())
}