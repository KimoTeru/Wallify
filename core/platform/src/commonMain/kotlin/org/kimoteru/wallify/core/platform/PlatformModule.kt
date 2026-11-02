package org.kimoteru.wallify.core.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchersImpl
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.core.platform")
class PlatformModule {
    @Singleton
    fun appDispatchers(): AppDispatchers = AppDispatchersImpl()

    @Singleton
    fun coroutineScope(dispatchers: AppDispatchers): CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.io)
}