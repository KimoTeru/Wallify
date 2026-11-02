package org.kimoteru.wallify.core.datastore.storage

import java.io.File
import org.koin.core.annotation.Singleton

@Singleton
actual class DataStorePathProvider {
    actual fun producePath(): String {
        val directory = File(System.getProperty("user.home"), ".wallify")
        directory.mkdirs()
        return directory.resolve(PREFERENCES_FILE_NAME).absolutePath
    }
}