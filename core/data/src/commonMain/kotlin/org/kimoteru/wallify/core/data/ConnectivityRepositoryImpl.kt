package org.kimoteru.wallify.core.data

import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.domain.ConnectivityRepository
import org.kimoteru.wallify.core.network.monitor.NetworkMonitor
import org.koin.core.annotation.Singleton

@Singleton
class ConnectivityRepositoryImpl(
    private val monitor: NetworkMonitor
) : ConnectivityRepository {
    override val isOnline: StateFlow<Boolean> = monitor.isOnline

    override fun start() = monitor.start()

    override fun stop() = monitor.stop()
}