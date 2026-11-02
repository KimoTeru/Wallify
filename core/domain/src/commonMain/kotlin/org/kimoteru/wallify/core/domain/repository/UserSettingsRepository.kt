package org.kimoteru.wallify.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.settings.UserSettings

interface UserSettingsRepository {
    fun observe(): Flow<UserSettings>

    suspend fun get(): Response<UserSettings>

    suspend fun update(settings: UserSettings): Response<UserSettings>
}