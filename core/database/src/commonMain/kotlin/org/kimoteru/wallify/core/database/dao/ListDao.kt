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

    /**
     * Ids saved at the positions [fromPosition] until [toPosition] of the list, in order. A write compares them with
     * the new ones first: Room invalidates every reader of `list_entry` on any write, even of equal rows.
     */
    @Query(
        "SELECT itemId FROM list_entry WHERE listKey = :listKey AND position >= :fromPosition " +
            "AND position < :toPosition ORDER BY position ASC"
    )
    suspend fun getItemIds(listKey: String, fromPosition: Int, toPosition: Int): List<String>

    /** Freshness of a list or of a single item (see [ListStateEntity]). */
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

    /** Drops the order of the lists last loaded before [threshold]; their states go with [deleteStatesBefore]. */
    @Query(
        "DELETE FROM list_entry WHERE listKey IN " +
            "(SELECT listKey FROM list_state WHERE updatedAtEpochMillis < :threshold)"
    )
    suspend fun deleteEntriesOfStatesBefore(threshold: Long)

    @Query("DELETE FROM list_state WHERE updatedAtEpochMillis < :threshold")
    suspend fun deleteStatesBefore(threshold: Long)

    @Query("DELETE FROM list_entry")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM list_state")
    suspend fun deleteAllStates()
}