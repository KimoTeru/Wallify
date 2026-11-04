package org.kimoteru.wallify.core.data.cache

import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.entity.ListEntryEntity
import org.kimoteru.wallify.core.database.entity.ListStateEntity
import org.kimoteru.wallify.core.platform.time.TimeProvider

internal class FakeListDao : ListDao {
    val entries = mutableListOf<ListEntryEntity>()
    val states = mutableMapOf<String, Long>()

    fun itemIds(listKey: String): List<String> =
        entries.filter { it.listKey == listKey }.sortedBy { it.position }.map { it.itemId }

    override suspend fun insertEntries(entries: List<ListEntryEntity>) {
        entries.forEach { entry ->
            this.entries.removeAll { it.listKey == entry.listKey && it.position == entry.position }
            this.entries.add(entry)
        }
    }

    override suspend fun deleteEntriesFrom(listKey: String, fromPosition: Int) {
        entries.removeAll { it.listKey == listKey && it.position >= fromPosition }
    }

    override suspend fun countEntries(listKey: String): Int = entries.count { it.listKey == listKey }

    override suspend fun upsertState(state: ListStateEntity) {
        states[state.listKey] = state.updatedAtEpochMillis
    }

    override suspend fun getUpdatedAt(listKey: String): Long? = states[listKey]

    override suspend fun deleteStatesByPrefix(prefix: String) {
        states.keys.removeAll { it.startsWith(prefix) }
    }

    override suspend fun deleteAllEntries() {
        entries.clear()
    }

    override suspend fun deleteAllStates() {
        states.clear()
    }
}

internal class FakeTimeProvider(var now: Long = 0L) : TimeProvider {
    override fun nowEpochMillis(): Long = now
}