package org.kimoteru.wallify.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.annotation.Singleton

@Singleton
actual class DatabaseBuilder(
    private val context: Context
) {
    actual fun builder(): RoomDatabase.Builder<AppDatabase> {
        val appContext = context.applicationContext
        val file = appContext.getDatabasePath(AppDatabase.FILE_NAME)
        file.parentFile?.mkdirs()
        return Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = file.absolutePath
        )
    }
}