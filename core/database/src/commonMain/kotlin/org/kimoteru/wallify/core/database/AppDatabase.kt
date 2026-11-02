package org.kimoteru.wallify.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [CacheEntryEntity::class], version = 1, exportSchema = true)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheEntryDao(): CacheEntryDao

    companion object {
        const val FILE_NAME = "wallify.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}