package org.kimoteru.wallify.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.database.entity.PhotoEntity

@Dao
interface PhotoDao {
    @Upsert
    suspend fun upsertAll(photos: List<PhotoEntity>)

    @Query("SELECT * FROM photo WHERE id = :id")
    fun observe(id: String): Flow<PhotoEntity?>

    @Query("SELECT * FROM photo WHERE id = :id")
    suspend fun get(id: String): PhotoEntity?

    @Query("SELECT * FROM photo WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<PhotoEntity>

    /** The first [limit] photos of the list [listKey] in the list's order. */
    @Query(
        "SELECT photo.* FROM list_entry INNER JOIN photo ON photo.id = list_entry.itemId " +
            "WHERE list_entry.listKey = :listKey ORDER BY list_entry.position ASC LIMIT :limit"
    )
    suspend fun getList(listKey: String, limit: Int): List<PhotoEntity>

    @Query(
        "SELECT photo.* FROM list_entry INNER JOIN photo ON photo.id = list_entry.itemId " +
            "WHERE list_entry.listKey = :listKey ORDER BY list_entry.position ASC"
    )
    fun pagingSource(listKey: String): PagingSource<Int, PhotoEntity>

    @Query("UPDATE photo SET currentUserCollectionIds = :collectionIds WHERE id = :id")
    suspend fun updateCollectionIds(id: String, collectionIds: List<Int>)

    /** Drops the photos that no saved list holds any more (their lists were pruned). */
    @Query("DELETE FROM photo WHERE id NOT IN (SELECT itemId FROM list_entry)")
    suspend fun deleteUnlisted()

    @Query("DELETE FROM photo")
    suspend fun deleteAll()
}