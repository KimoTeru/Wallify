package org.kimoteru.wallify.core.storage

import okio.Path

expect class CacheDirectoryProvider {
    fun directory(): Path
}