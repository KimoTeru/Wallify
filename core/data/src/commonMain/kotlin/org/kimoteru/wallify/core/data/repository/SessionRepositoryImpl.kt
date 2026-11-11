package org.kimoteru.wallify.core.data.repository

import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.datastore.prefs.DataStorePrefs
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.koin.core.annotation.Singleton

@Singleton
class SessionRepositoryImpl(
    private val prefs: DataStorePrefs,
    private val dispatchers: AppDispatchers
) : SessionRepository {
    private val tokenKey = stringPreferencesKey("auth_token")

    override val authToken: Flow<String?> = prefs.getNullableFlow(tokenKey).flowOn(dispatchers.io)

    override suspend fun updateAuthToken(token: String?): Unit = withContext(dispatchers.io) {
        if (token.isNullOrBlank()) {
            prefs.remove(tokenKey)
        } else {
            prefs.save(tokenKey, token)
        }
    }

    override suspend fun clear(): Unit = withContext(dispatchers.io) {
        prefs.remove(tokenKey)
    }
}