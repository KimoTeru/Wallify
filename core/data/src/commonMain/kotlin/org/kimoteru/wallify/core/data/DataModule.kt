package org.kimoteru.wallify.core.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.data.cache.CacheDatabaseCleaner
import org.kimoteru.wallify.core.data.download.FileDownloader
import org.kimoteru.wallify.core.data.session.SessionDataCleaner
import org.kimoteru.wallify.core.data.session.SessionHeadersSynchronizer
import org.kimoteru.wallify.core.data.session.UserSessionSynchronizer
import org.kimoteru.wallify.core.domain.manager.UserSessionManager
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.repository.RequestStatsRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.domain.repository.UserSettingsRepository
import org.kimoteru.wallify.core.domain.usecase.settings.ObserveUserSettingsUseCase
import org.kimoteru.wallify.core.domain.usecase.settings.UpdateUserSettingsUseCase
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository
import org.kimoteru.wallify.core.network.session.SessionExpiryCollector
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

    @Singleton(createdAtStart = true)
    fun userSessionManager(
        headersCollector: HeadersCollectorRepository,
        sessionExpiry: SessionExpiryCollector,
        userRepository: UserRepository,
        session: SessionRepository,
        connectivityRepository: ConnectivityRepository,
        scope: CoroutineScope
    ): UserSessionManager = UserSessionSynchronizer(
        headersCollector = headersCollector,
        sessionExpiry = sessionExpiry,
        userRepository = userRepository,
        session = session,
        connectivityRepository = connectivityRepository,
        scope = scope
    ).also { it.start() }

    @Singleton(createdAtStart = true)
    fun sessionDataCleaner(
        session: SessionRepository,
        databaseCleaner: CacheDatabaseCleaner,
        fileDownloader: FileDownloader,
        userRepository: UserRepository,
        requestStatsRepository: RequestStatsRepository,
        scope: CoroutineScope
    ): SessionDataCleaner = SessionDataCleaner(
        session = session,
        databaseCleaner = databaseCleaner,
        fileDownloader = fileDownloader,
        userRepository = userRepository,
        requestStatsRepository = requestStatsRepository,
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