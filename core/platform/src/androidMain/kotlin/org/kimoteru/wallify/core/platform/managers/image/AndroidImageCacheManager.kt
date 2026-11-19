package org.kimoteru.wallify.core.platform.managers.image

import android.content.Context
import coil3.SingletonImageLoader
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.koin.core.annotation.Singleton

@Singleton
class AndroidImageCacheManager(
    private val context: Context,
    private val dispatchers: AppDispatchers,
) : ImageCacheManager {
    override suspend fun clear(): Unit = withContext(dispatchers.io) {
        val imageLoader = SingletonImageLoader.get(context)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}