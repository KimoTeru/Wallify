package org.kimoteru.wallify.core.data.settings

import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.model.settings.UserSettings

interface UserSettingsLocalDataSource {
    fun observe(): Flow<UserSettings>

    suspend fun read(): UserSettings

    suspend fun write(settings: UserSettings)
}