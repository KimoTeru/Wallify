package org.kimoteru.wallify.core.data.repository

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.network.monitor.NetworkMonitor
import org.koin.core.annotation.Singleton

/** A switch of network (Wi-Fi to mobile) or a network that is still being validated goes offline for a moment. */
internal val OFFLINE_GRACE: Duration = 3.seconds

/**
 * Offline counts only once it has lasted [OFFLINE_GRACE], online counts at once: a short drop would flash the
 * no-internet snackbar of the root and restart what waits for the connection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ConnectivityRepositoryImpl(
    private val monitor: NetworkMonitor,
    scope: CoroutineScope
) : ConnectivityRepository {
    override val isOnline: StateFlow<Boolean> = monitor.isOnline
        .mapLatest { online ->
            if (!online) delay(OFFLINE_GRACE)
            online
        }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = monitor.isOnline.value)

    override fun start() = monitor.start()

    override fun stop() = monitor.stop()
}