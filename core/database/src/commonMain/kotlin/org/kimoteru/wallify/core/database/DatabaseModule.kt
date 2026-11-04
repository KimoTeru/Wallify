package org.kimoteru.wallify.core.database

import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.database.builder.DatabaseBuilder
import org.kimoteru.wallify.core.database.builder.build
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.dao.TopicDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.core.database")
class DatabaseModule {
    @Singleton
    fun appDatabase(builder: DatabaseBuilder, appDispatchers: AppDispatchers): AppDatabase =
        builder.build(appDispatchers)

    @Singleton
    fun photoDao(database: AppDatabase): PhotoDao = database.photoDao()

    @Singleton
    fun userDao(database: AppDatabase): UserDao = database.userDao()

    @Singleton
    fun collectionDao(database: AppDatabase): CollectionDao = database.collectionDao()

    @Singleton
    fun topicDao(database: AppDatabase): TopicDao = database.topicDao()

    @Singleton
    fun statsDao(database: AppDatabase): StatsDao = database.statsDao()

    @Singleton
    fun listDao(database: AppDatabase): ListDao = database.listDao()
}