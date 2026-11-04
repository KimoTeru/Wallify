package org.kimoteru.wallify.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Response

/**
 * Sign-in with the user's account. The token is kept by `SessionRepository`, from where it reaches the requests
 * as `Bearer`.
 */
interface AuthRepository {

    val isSignedIn: Flow<Boolean>

    /**
     * The whole sign-in: opens the authorization page in the system browser, waits until the user comes back,
     * exchanges the code for the token and keeps it. Takes as long as the user needs (up to 5 minutes);
     * cancel the caller to stop waiting.
     *
     * Returns `Success(true)` when the user is signed in, `Success(false)` when the user declined or did not come
     * back in time, `Error` when the browser could not be opened or the backend refused the code.
     * On `false` and on error the previous session stays as it was.
     */
    suspend fun signIn(): Response<Boolean>

    suspend fun signOut()
}