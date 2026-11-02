package org.kimoteru.wallify.core.data.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.kimoteru.wallify.core.datastore.prefs.DataStorePrefs
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.koin.core.annotation.Singleton

@Singleton
class DataStoreUserSettingsDataSource(
    private val prefs: DataStorePrefs,
    private val json: Json
) : UserSettingsLocalDataSource {
    override fun observe(): Flow<UserSettings> = prefs.getNullableFlow(key).map { raw ->
        raw.toSettings()
    }

    override suspend fun read(): UserSettings = prefs.getNullable(key).toSettings()

    override suspend fun write(settings: UserSettings) {
        prefs.save(key, json.encodeToString(UserSettings.serializer(), settings))
    }

    private fun String?.toSettings(): UserSettings {
        if (this.isNullOrBlank()) return UserSettings.Default
        return json.decodeFromString(UserSettings.serializer(), this)
    }

    private companion object {
        val key = stringPreferencesKey("user_settings")
    }
}