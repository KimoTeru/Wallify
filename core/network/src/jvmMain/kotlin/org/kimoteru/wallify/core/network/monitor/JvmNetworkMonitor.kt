package org.kimoteru.wallify.core.network.monitor

import java.net.NetworkInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Singleton

@Singleton
internal class JvmNetworkMonitor : NetworkMonitor {
    private val onlineState = MutableStateFlow(readOnline())
    override val isOnline: StateFlow<Boolean> = onlineState.asStateFlow()

    override suspend fun checkInternet() {
        onlineState.value = readOnline()
    }

    override fun start() {
        onlineState.value = readOnline()
    }

    override fun stop() = Unit

    private fun readOnline(): Boolean = try {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
        interfaces.asSequence().any { networkInterface ->
            networkInterface.isUp && !networkInterface.isLoopback
        }
    } catch (_: Exception) {
        false
    }
}