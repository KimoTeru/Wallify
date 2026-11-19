package org.kimoteru.wallify.core.platform.managers.image

import coil3.PlatformContext
import coil3.SingletonImageLoader
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.koin.core.annotation.Singleton

@Singleton
class JvmImageCacheManager(
    private val dispatchers: AppDispatchers,
) : ImageCacheManager {
    override suspend fun clear(): Unit = withContext(dispatchers.io) {
        val imageLoader = SingletonImageLoader.get(PlatformContext.INSTANCE)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}