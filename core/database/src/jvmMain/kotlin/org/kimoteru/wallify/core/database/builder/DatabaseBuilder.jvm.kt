package org.kimoteru.wallify.core.database.builder

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File
import org.kimoteru.wallify.core.database.AppDatabase
import org.koin.core.annotation.Singleton

@Singleton
actual class DatabaseBuilder {
    actual fun builder(): RoomDatabase.Builder<AppDatabase> {
        val directory = File(System.getProperty("user.home"), ".wallify")
        directory.mkdirs()
        val file = File(directory, AppDatabase.FILE_NAME)
        return Room.databaseBuilder<AppDatabase>(name = file.absolutePath)
    }
}