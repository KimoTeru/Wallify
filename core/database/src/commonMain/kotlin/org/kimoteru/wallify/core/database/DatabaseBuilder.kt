package org.kimoteru.wallify.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers

expect class DatabaseBuilder {
    fun builder(): RoomDatabase.Builder<AppDatabase>
}

fun DatabaseBuilder.build(appDispatchers: AppDispatchers): AppDatabase =
    builder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(appDispatchers.io)
        .build()