package org.kimoteru.wallify.core.network.monitor

import kotlinx.coroutines.flow.StateFlow

/**
 * Whether the internet is reachable. Android follows the validated default network, desktop checks that the API host
 * answers. [isOnline] is kept up to date only between [start] and [stop]; [start] reads the state again.
 */
interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>

    fun start()

    fun stop()
}