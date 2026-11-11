package org.kimoteru.wallify.core.data.cache

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.entity.ListEntryEntity
import org.kimoteru.wallify.core.database.entity.ListStateEntity
import org.kimoteru.wallify.core.platform.time.TimeProvider

internal class FakeListDao : ListDao {
    val entries = mutableListOf<ListEntryEntity>()
    val states = mutableMapOf<String, Long>()

    /** How many times rows were written: every write invalidates the lists on screen, so equal rows must not. */
    var insertCalls = 0

    fun itemIds(listKey: String): List<String> =
        entries.filter { it.listKey == listKey }.sortedBy { it.position }.map { it.itemId }

    override suspend fun insertEntries(entries: List<ListEntryEntity>) {
        insertCalls++
        entries.forEach { entry ->
            this.entries.removeAll { it.listKey == entry.listKey && it.position == entry.position }
            this.entries.add(entry)
        }
    }

    override suspend fun deleteEntriesFrom(listKey: String, fromPosition: Int) {
        entries.removeAll { it.listKey == listKey && it.position >= fromPosition }
    }

    override suspend fun countEntries(listKey: String): Int = entries.count { it.listKey == listKey }

    override suspend fun getItemIds(listKey: String, fromPosition: Int, toPosition: Int): List<String> =
        entries.filter { it.listKey == listKey && it.position in fromPosition until toPosition }
            .sortedBy { it.position }
            .map { it.itemId }

    override suspend fun upsertState(state: ListStateEntity) {
        states[state.listKey] = state.updatedAtEpochMillis
    }

    override suspend fun getUpdatedAt(listKey: String): Long? = states[listKey]

    override suspend fun deleteStatesByPrefix(prefix: String) {
        states.keys.removeAll { it.startsWith(prefix) }
    }

    override suspend fun deleteEntriesOfStatesBefore(threshold: Long) {
        val oldKeys = states.filterValues { it < threshold }.keys
        entries.removeAll { it.listKey in oldKeys }
    }

    override suspend fun deleteStatesBefore(threshold: Long) {
        states.values.removeAll { it < threshold }
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

/** Runs everything at once in the test: the work the data layer moves to `io` stays deterministic. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class FakeAppDispatchers : AppDispatchers {
    private val dispatcher = UnconfinedTestDispatcher()

    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val unconfined: CoroutineDispatcher = dispatcher
}