package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entry")
data class CacheEntryEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAtEpochMillis: Long
)