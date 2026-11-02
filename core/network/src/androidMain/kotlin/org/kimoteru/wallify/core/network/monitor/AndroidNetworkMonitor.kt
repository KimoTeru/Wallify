package org.kimoteru.wallify.core.network.monitor

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.koin.core.annotation.Singleton

@Singleton
internal class AndroidNetworkMonitor(
    context: Context,
    appDispatchers: AppDispatchers
) : NetworkMonitor {
    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    private val scope = CoroutineScope(SupervisorJob() + appDispatchers.default)
    private val lock = Any()

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private val onlineState = MutableStateFlow(readOnline())
    override val isOnline: StateFlow<Boolean> = onlineState.asStateFlow()

    private var started = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onlineState.value = readOnline()
        }

        override fun onLost(network: Network) {
            onlineState.value = readOnline()
        }

        override fun onUnavailable() {
            onlineState.value = readOnline()
        }
    }

    override suspend fun checkInternet() {
        var attempts = 0
        while (!onlineState.value && attempts < MAX_ATTEMPTS) {
            delay(RETRY_DELAY_MILLIS)
            attempts += 1
            onlineState.value = readOnline()
        }
    }

    override fun start() {
        val shouldCheck = synchronized(lock) {
            if (started) return
            try {
                connectivityManager.registerNetworkCallback(networkRequest, callback)
                started = true
                true
            } catch (_: RuntimeException) {
                false
            }
        }
        if (shouldCheck) {
            scope.launch { checkInternet() }
        }
    }

    override fun stop() {
        synchronized(lock) {
            if (!started) return
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: IllegalArgumentException) {
            } finally {
                started = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun readOnline(): Boolean = try {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    } catch (_: RuntimeException) {
        false
    }

    private companion object {
        const val RETRY_DELAY_MILLIS = 1_000L
        const val MAX_ATTEMPTS = 5
    }
}