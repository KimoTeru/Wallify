package org.kimoteru.wallify.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.cache.StringMemoryCache
import org.kimoteru.wallify.core.database.CacheEntryDao
import org.kimoteru.wallify.core.database.CacheEntryEntity
import org.kimoteru.wallify.core.platform.time.TimeProvider

class KeyValueRepositoryImplTest {

    @Test
    fun putThenObserveReturnsValue() = runTest {
        val repository = KeyValueRepositoryImpl(
            dao = FakeCacheEntryDao(),
            memory = StringMemoryCache(),
            time = FakeTimeProvider(fixedNowEpochMillis = 1_700_000_000_000L)
        )

        repository.put(key = "theme", value = "dark")

        assertEquals("dark", repository.observe("theme").first())
    }
}

private class FakeCacheEntryDao : CacheEntryDao {
    private val entries = MutableStateFlow<Map<String, CacheEntryEntity>>(emptyMap())

    override fun observe(key: String): Flow<CacheEntryEntity?> = entries.map { it[key] }

    override suspend fun get(key: String): CacheEntryEntity? = entries.value[key]

    override suspend fun upsert(entity: CacheEntryEntity) {
        entries.update { it + (entity.key to entity) }
    }

    override suspend fun delete(key: String) {
        entries.update { it - key }
    }
}

private class FakeTimeProvider(
    private val fixedNowEpochMillis: Long
) : TimeProvider {
    override fun nowEpochMillis(): Long = fixedNowEpochMillis
}