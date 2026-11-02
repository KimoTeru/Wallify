package org.kimoteru.wallify.core.domain.repository

import kotlinx.coroutines.flow.Flow

/** Stores the signed-in user's OAuth access token. Null means the app acts as an anonymous client. */
interface SessionRepository {
    val authToken: Flow<String?>

    suspend fun updateAuthToken(token: String?)

    suspend fun clear()
}