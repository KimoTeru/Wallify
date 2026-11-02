package org.kimoteru.wallify.core.domain

import kotlinx.coroutines.flow.Flow

interface KeyValueRepository {
    fun observe(key: String): Flow<String?>
    suspend fun put(key: String, value: String)
    suspend fun remove(key: String)
}