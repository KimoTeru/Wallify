package org.kimoteru.wallify.core.platform.time

import kotlin.time.Clock
import org.koin.core.annotation.Singleton

interface TimeProvider {
    fun nowEpochMillis(): Long
}

@Singleton
class SystemTimeProvider : TimeProvider {
    override fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
}