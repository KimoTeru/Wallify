package org.kimoteru.wallify.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.kimoteru.wallify.core.cache.StringMemoryCache
import org.kimoteru.wallify.core.database.CacheEntryDao
import org.kimoteru.wallify.core.database.CacheEntryEntity
import org.kimoteru.wallify.core.domain.KeyValueRepository
import org.kimoteru.wallify.core.platform.time.TimeProvider
import org.koin.core.annotation.Singleton

@Singleton
class KeyValueRepositoryImpl(
    private val dao: CacheEntryDao,
    private val memory: StringMemoryCache,
    private val time: TimeProvider
) : KeyValueRepository {
    override fun observe(key: String): Flow<String?> = flow {
        memory.get(key)?.let { cached -> emit(cached) }
        dao.observe(key).collect { entity ->
            val value = entity?.value
            if (value == null) {
                memory.remove(key)
            } else {
                memory.put(key, value)
            }
            emit(value)
        }
    }

    override suspend fun put(key: String, value: String) {
        memory.put(key, value)
        dao.upsert(
            CacheEntryEntity(
                key = key,
                value = value,
                updatedAtEpochMillis = time.nowEpochMillis()
            )
        )
    }

    override suspend fun remove(key: String) {
        memory.remove(key)
        dao.delete(key)
    }
}