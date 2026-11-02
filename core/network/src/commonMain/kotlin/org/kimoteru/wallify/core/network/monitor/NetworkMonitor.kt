package org.kimoteru.wallify.core.network.monitor

import kotlinx.coroutines.flow.StateFlow

/**
 * Connectivity observer.
 * Android uses ConnectivityManager, desktop checks network interfaces,
 * iOS is a stub until a path monitor is wired in the app target.
 */
interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>

    suspend fun checkInternet()

    fun start()

    fun stop()
}