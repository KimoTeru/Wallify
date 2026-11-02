package org.kimoteru.wallify.core.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Internet availability. Call [start] while the app is in the foreground and [stop] when it is paused. */
interface ConnectivityRepository {
    val isOnline: StateFlow<Boolean>

    fun start()

    fun stop()
}