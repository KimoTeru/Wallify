package org.kimoteru.wallify.core.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.domain.SessionRepository
import org.kimoteru.wallify.core.domain.repository.UserSettingsRepository
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.domain.usecase.settings.UpdateUserSettingsUseCase
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * Data layer wiring. Repositories and data sources are annotated where they are declared;
 * what cannot carry an annotation (domain use cases, third-party types) is provided here.
 * Add repository bindings for the backend API next to the other implementations.
 */
@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.core.data")
class DataModule {
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Singleton(createdAtStart = true)
    fun sessionHeadersSynchronizer(
        session: SessionRepository,
        settings: UserSettingsRepository,
        headersCollector: HeadersCollectorRepository,
        scope: CoroutineScope
    ): SessionHeadersSynchronizer = SessionHeadersSynchronizer(
        session = session,
        settings = settings,
        headersCollector = headersCollector,
        scope = scope
    ).also { it.start() }

    @Factory
    fun observeUserSettingsUseCase(
        repository: UserSettingsRepository,
        dispatchers: AppDispatchers
    ): ObserveUserSettingsUseCase = ObserveUserSettingsUseCase(repository, dispatchers)

    @Factory
    fun updateUserSettingsUseCase(
        repository: UserSettingsRepository,
        dispatchers: AppDispatchers
    ): UpdateUserSettingsUseCase = UpdateUserSettingsUseCase(repository, dispatchers)
}