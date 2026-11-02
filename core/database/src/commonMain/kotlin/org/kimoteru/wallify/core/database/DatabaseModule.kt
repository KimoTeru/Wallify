package org.kimoteru.wallify.core.database

import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.database.builder.DatabaseBuilder
import org.kimoteru.wallify.core.database.builder.build
import org.kimoteru.wallify.core.database.dao.CacheEntryDao
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
    fun cacheEntryDao(database: AppDatabase): CacheEntryDao = database.cacheEntryDao()
}