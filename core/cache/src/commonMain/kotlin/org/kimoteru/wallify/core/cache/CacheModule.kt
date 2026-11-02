package org.kimoteru.wallify.core.cache

import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.core.cache")
class CacheModule {
    @Singleton
    fun diskCache(
        directoryProvider: CacheDirectoryProvider,
        appDispatchers: AppDispatchers
    ): DiskCache = DiskCache(
        directory = directoryProvider.directory(),
        appDispatchers = appDispatchers
    )
}