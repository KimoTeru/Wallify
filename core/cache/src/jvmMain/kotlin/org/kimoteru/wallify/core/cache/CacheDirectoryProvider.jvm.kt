package org.kimoteru.wallify.core.cache

import java.io.File
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.annotation.Singleton

@Singleton
actual class CacheDirectoryProvider {
    actual fun directory(): Path {
        val directory = File(System.getProperty("user.home"), ".wallify/$CACHE_DIRECTORY_NAME")
        directory.mkdirs()
        return directory.absolutePath.toPath()
    }

    private companion object {
        const val CACHE_DIRECTORY_NAME = "wallify-cache"
    }
}