package org.kimoteru.wallify.core.data.repository

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.network.monitor.NetworkMonitor

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityRepositoryImplTest {

    @Test
    fun shortDropDoesNotGoOffline() = runTest(UnconfinedTestDispatcher()) {
        val monitor = FakeNetworkMonitor(initial = true)
        val repository = ConnectivityRepositoryImpl(monitor, backgroundScope)

        monitor.isOnline.value = false
        advanceTimeBy(OFFLINE_GRACE - 1.milliseconds)
        monitor.isOnline.value = true
        advanceTimeBy(OFFLINE_GRACE)

        assertTrue(repository.isOnline.value)
    }

    @Test
    fun lastingDropGoesOfflineAfterTheGrace() = runTest(UnconfinedTestDispatcher()) {
        val monitor = FakeNetworkMonitor(initial = true)
        val repository = ConnectivityRepositoryImpl(monitor, backgroundScope)

        monitor.isOnline.value = false
        advanceTimeBy(OFFLINE_GRACE - 1.milliseconds)
        assertTrue(repository.isOnline.value)

        advanceTimeBy(2.milliseconds)
        assertFalse(repository.isOnline.value)
    }

    @Test
    fun returnOfTheConnectionIsImmediate() = runTest(UnconfinedTestDispatcher()) {
        val monitor = FakeNetworkMonitor(initial = false)
        val repository = ConnectivityRepositoryImpl(monitor, backgroundScope)

        monitor.isOnline.value = true

        assertTrue(repository.isOnline.value)
    }
}

private class FakeNetworkMonitor(initial: Boolean) : NetworkMonitor {
    override val isOnline: MutableStateFlow<Boolean> = MutableStateFlow(initial)

    override fun start() = Unit

    override fun stop() = Unit
}