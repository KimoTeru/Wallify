package org.kimoteru.wallify.core.cache

import okio.Path

expect class CacheDirectoryProvider {
    fun directory(): Path
}