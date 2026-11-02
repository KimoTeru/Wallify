package org.kimoteru.wallify.core.cache

import android.content.Context
import java.io.File
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.annotation.Singleton

@Singleton
actual class CacheDirectoryProvider(
    private val context: Context
) {
    actual fun directory(): Path {
        val directory = File(context.cacheDir, CACHE_DIRECTORY_NAME)
        directory.mkdirs()
        return directory.absolutePath.toPath()
    }

    private companion object {
        const val CACHE_DIRECTORY_NAME = "wallify-cache"
    }
}