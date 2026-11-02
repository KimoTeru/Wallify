package org.kimoteru.wallify.core.datastore.storage

import android.content.Context
import org.koin.core.annotation.Singleton

@Singleton
actual class DataStorePathProvider(
    private val context: Context
) {
    actual fun producePath(): String =
        context.filesDir.resolve(PREFERENCES_FILE_NAME).absolutePath
}