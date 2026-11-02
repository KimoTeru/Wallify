package org.kimoteru.wallify.core.network.monitor

import java.net.NetworkInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.koin.core.annotation.Singleton

/**
 * Reading network interfaces blocks (the first call is slow on desktop systems), so it never runs on the caller's
 * thread. The state starts as online and is corrected as soon as the first read finishes.
 */
@Singleton
internal class JvmNetworkMonitor(
    private val appDispatchers: AppDispatchers
) : NetworkMonitor {
    private val scope = CoroutineScope(SupervisorJob() + appDispatchers.io)

    override val isOnline: StateFlow<Boolean>
        field = MutableStateFlow(true)

    init {
        refresh()
    }

    override suspend fun checkInternet() {
        isOnline.value = withContext(appDispatchers.io) {
            readOnline()
        }
    }

    override fun start() = refresh()

    override fun stop() = Unit

    private fun refresh() {
        scope.launch { isOnline.value = readOnline() }
    }

    private fun readOnline(): Boolean = try {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
        interfaces.asSequence().any { networkInterface ->
            networkInterface.isUp && !networkInterface.isLoopback
        }
    } catch (_: Exception) {
        false
    }
}