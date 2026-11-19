package org.kimoteru.wallify.core.platform.managers.image

/** Empties the memory and disk cache of the images. Suspends, because the disk part does file I/O. */
interface ImageCacheManager {
    suspend fun clear()
}