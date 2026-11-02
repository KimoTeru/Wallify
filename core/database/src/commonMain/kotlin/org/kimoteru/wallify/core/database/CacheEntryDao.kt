package org.kimoteru.wallify.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheEntryDao {
    @Query("SELECT * FROM cache_entry WHERE `key` = :key")
    fun observe(key: String): Flow<CacheEntryEntity?>

    @Query("SELECT * FROM cache_entry WHERE `key` = :key")
    suspend fun get(key: String): CacheEntryEntity?

    @Upsert
    suspend fun upsert(entity: CacheEntryEntity)

    @Query("DELETE FROM cache_entry WHERE `key` = :key")
    suspend fun delete(key: String)
}