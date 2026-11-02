package org.kimoteru.wallify.core.data

import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.datastore.DataStorePrefs
import org.kimoteru.wallify.core.domain.SessionRepository
import org.koin.core.annotation.Singleton

@Singleton
class SessionRepositoryImpl(
    private val prefs: DataStorePrefs
) : SessionRepository {
    private val tokenKey = stringPreferencesKey("auth_token")

    override val authToken: Flow<String?> = prefs.getNullableFlow(tokenKey)

    override suspend fun updateAuthToken(token: String?) {
        if (token.isNullOrBlank()) {
            prefs.remove(tokenKey)
        } else {
            prefs.save(tokenKey, token)
        }
    }

    override suspend fun clear() {
        prefs.remove(tokenKey)
    }
}