package org.kimoteru.wallify.core.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Singleton

open class MemoryCache<K, V> {
    private val mutex = Mutex()
    private val map = mutableMapOf<K, V>()

    suspend fun get(key: K): V? = mutex.withLock { map[key] }

    suspend fun put(key: K, value: V) {
        mutex.withLock {
            map[key] = value
        }
    }

    suspend fun remove(key: K) {
        mutex.withLock {
            map.remove(key)
        }
    }

    suspend fun clear() {
        mutex.withLock {
            map.clear()
        }
    }
}

@Singleton
class StringMemoryCache : MemoryCache<String, String>()