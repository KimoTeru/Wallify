package org.kimoteru.wallify.core.network.monitor

import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.network.config.API_BASE_URL
import org.koin.core.annotation.Singleton

/**
 * The JVM has no system connectivity callback, and an interface that is up says nothing about the internet (VPN,
 * Docker and VM adapters are always up). So while it is started, the monitor opens a TCP connection to the API host
 * every [CHECK_INTERVAL]: online is the backend the app talks to being reachable. The check blocks, so it runs on
 * the IO dispatcher. The state starts as online and is corrected by the first check.
 */
@Singleton
internal class JvmNetworkMonitor(
    appDispatchers: AppDispatchers
) : NetworkMonitor {
    private val scope = CoroutineScope(SupervisorJob() + appDispatchers.io)
    private val host = URI(API_BASE_URL).host

    override val isOnline: StateFlow<Boolean>
        field = MutableStateFlow(true)

    private var job: Job? = null

    @Synchronized
    override fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                val reachable = isReachable()
                // The connect cannot be interrupted: a check that outlived stop() must not overwrite a newer one.
                if (isActive) isOnline.value = reachable
                delay(CHECK_INTERVAL)
            }
        }
    }

    @Synchronized
    override fun stop() {
        job?.cancel()
        job = null
    }

    private fun isReachable(): Boolean = runCatching {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, HTTPS_PORT), CONNECT_TIMEOUT_MILLIS)
        }
    }.isSuccess

    private companion object {
        val CHECK_INTERVAL = 5.seconds
        const val CONNECT_TIMEOUT_MILLIS = 3_000
        const val HTTPS_PORT = 443
    }
}