package org.kimoteru.wallify.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.database.entity.CollectionEntity

@Dao
interface CollectionDao {
    @Upsert
    suspend fun upsert(collection: CollectionEntity)

    @Upsert
    suspend fun upsertAll(collections: List<CollectionEntity>)

    @Query("SELECT * FROM `collection` WHERE id = :id")
    fun observe(id: Int): Flow<CollectionEntity?>

    /** The first [limit] collections of the list [listKey] in the list's order. */
    @Query(
        "SELECT `collection`.* FROM list_entry " +
            "INNER JOIN `collection` ON `collection`.id = CAST(list_entry.itemId AS INTEGER) " +
            "WHERE list_entry.listKey = :listKey ORDER BY list_entry.position ASC LIMIT :limit"
    )
    fun observeList(listKey: String, limit: Int): Flow<List<CollectionEntity>>

    @Query(
        "SELECT `collection`.* FROM list_entry " +
            "INNER JOIN `collection` ON `collection`.id = CAST(list_entry.itemId AS INTEGER) " +
            "WHERE list_entry.listKey = :listKey ORDER BY list_entry.position ASC"
    )
    fun pagingSource(listKey: String): PagingSource<Int, CollectionEntity>

    @Query("DELETE FROM `collection` WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM `collection`")
    suspend fun deleteAll()
}