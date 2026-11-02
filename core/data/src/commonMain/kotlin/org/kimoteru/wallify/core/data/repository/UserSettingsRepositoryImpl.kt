package org.kimoteru.wallify.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.responseOf
import org.kimoteru.wallify.core.data.settings.UserSettingsLocalDataSource
import org.kimoteru.wallify.core.domain.repository.UserSettingsRepository
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.koin.core.annotation.Singleton

@Singleton
class UserSettingsRepositoryImpl(
    private val local: UserSettingsLocalDataSource,
    private val dispatchers: AppDispatchers
) : UserSettingsRepository {
    override fun observe(): Flow<UserSettings> = local.observe().flowOn(dispatchers.io)

    override suspend fun get(): Response<UserSettings> = withContext(dispatchers.io) {
        responseOf { local.read() }
    }

    override suspend fun update(settings: UserSettings): Response<UserSettings> =
        withContext(dispatchers.io) {
            responseOf {
                local.write(settings)
                settings
            }
        }
}