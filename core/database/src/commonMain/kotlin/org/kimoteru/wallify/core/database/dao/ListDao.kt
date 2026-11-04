package org.kimoteru.wallify.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import org.kimoteru.wallify.core.database.entity.ListEntryEntity
import org.kimoteru.wallify.core.database.entity.ListStateEntity

/** Order and freshness of the saved lists; the items themselves live in their own tables. */
@Dao
interface ListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<ListEntryEntity>)

    /** Drops the tail of a list after a refresh made it shorter. */
    @Query("DELETE FROM list_entry WHERE listKey = :listKey AND position >= :fromPosition")
    suspend fun deleteEntriesFrom(listKey: String, fromPosition: Int)

    @Query("SELECT COUNT(*) FROM list_entry WHERE listKey = :listKey")
    suspend fun countEntries(listKey: String): Int

    @Upsert
    suspend fun upsertState(state: ListStateEntity)

    @Query("SELECT updatedAtEpochMillis FROM list_state WHERE listKey = :listKey")
    suspend fun getUpdatedAt(listKey: String): Long?

    /**
     * Makes the lists whose key starts with [prefix] stale: they are loaded again the next time they are opened,
     * while the saved items stay on screen until the new ones arrive. `substr`, not `LIKE`, so `_` and `%` in a key
     * stay plain.
     */
    @Query("DELETE FROM list_state WHERE substr(listKey, 1, length(:prefix)) = :prefix")
    suspend fun deleteStatesByPrefix(prefix: String)

    @Query("DELETE FROM list_entry")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM list_state")
    suspend fun deleteAllStates()
}