package org.kimoteru.wallify.core.network.monitor

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Singleton

/**
 * Follows the default network of the app (`registerDefaultNetworkCallback`): online is a default network that the
 * system validated, so it really reaches the internet. The capabilities come from [ConnectivityManager.NetworkCallback]
 * itself: reading them synchronously inside the callback races with the system.
 */
@Singleton
internal class AndroidNetworkMonitor(
    context: Context
) : NetworkMonitor {
    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    override val isOnline: StateFlow<Boolean>
        field = MutableStateFlow(readOnline())

    private var started = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            isOnline.value = capabilities.hasInternet()
        }

        override fun onLost(network: Network) {
            // A new default network, if any, follows with its own capabilities.
            isOnline.value = false
        }
    }

    @Synchronized
    override fun start() {
        if (started) return
        // The state may be stale after a pause; the callback corrects it as soon as it is registered.
        isOnline.value = readOnline()
        started = runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }.isSuccess
    }

    @Synchronized
    override fun stop() {
        if (!started) return
        started = false
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
    }

    @SuppressLint("MissingPermission")
    private fun readOnline(): Boolean = runCatching {
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasInternet() == true
    }.getOrDefault(false)

    private fun NetworkCapabilities.hasInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}