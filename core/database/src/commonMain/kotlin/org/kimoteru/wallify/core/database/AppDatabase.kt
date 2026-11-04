package org.kimoteru.wallify.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.kimoteru.wallify.core.database.converter.DatabaseConverters
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.dao.TopicDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.kimoteru.wallify.core.database.entity.CollectionEntity
import org.kimoteru.wallify.core.database.entity.ListEntryEntity
import org.kimoteru.wallify.core.database.entity.ListStateEntity
import org.kimoteru.wallify.core.database.entity.MonthStatsEntity
import org.kimoteru.wallify.core.database.entity.PhotoEntity
import org.kimoteru.wallify.core.database.entity.StatisticsEntity
import org.kimoteru.wallify.core.database.entity.TopicEntity
import org.kimoteru.wallify.core.database.entity.TotalStatsEntity
import org.kimoteru.wallify.core.database.entity.UserEntity

@Database(
    entities = [
        PhotoEntity::class,
        UserEntity::class,
        CollectionEntity::class,
        TopicEntity::class,
        StatisticsEntity::class,
        TotalStatsEntity::class,
        MonthStatsEntity::class,
        ListEntryEntity::class,
        ListStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun userDao(): UserDao
    abstract fun collectionDao(): CollectionDao
    abstract fun topicDao(): TopicDao
    abstract fun statsDao(): StatsDao
    abstract fun listDao(): ListDao

    companion object {
        const val FILE_NAME = "wallify.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}