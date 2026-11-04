package org.kimoteru.wallify.core.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.domain.manager.UserSessionManager
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository

/**
 * Keeps the signed-in user in `UserRepository.currentUser` in step with the session, so screens never load it.
 *
 * It follows the `Authorization` of [HeadersCollectorRepository], not the stored token: the user is requested only
 * once the HTTP client really sends the new token, otherwise the request would go out with the old one and fail.
 * A new token loads the user, a missing token clears it once the storage has none either (a sign-out), and a token
 * the backend rejects (401) ends the session.
 * Any other failure goes to [loadError] and the load is repeated when the connection returns after a drop.
 */
class UserSessionSynchronizer(
    private val headersCollector: HeadersCollectorRepository,
    private val userRepository: UserRepository,
    private val session: SessionRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val scope: CoroutineScope
) : UserSessionManager {

    override val loadError: StateFlow<ResponseError?>
        field = MutableStateFlow<ResponseError?>(null)

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            headersCollector.headersFlow
                .mapDistinctBy { headers -> headers.authorization }
                .collectLatest { authorization ->
                    loadError.value = null
                    if (authorization == null) {
                        clearUserIfSignedOut()
                    } else {
                        loadUser()
                    }
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    /**
     * The client starts without a token and gets it from the storage a moment later, so "no token" alone does not
     * mean a sign-out. Only when the storage holds no token either is the session over; otherwise the saved user stays,
     * and the app opens with it even without a connection.
     */
    private suspend fun clearUserIfSignedOut() {
        if (session.authToken.first().isNullOrBlank()) {
            userRepository.clearCurrentUser()
        }
    }

    private suspend fun loadUser() {
        while (true) {
            loadError.value = null

            when (val response = userRepository.refreshCurrentUser()) {
                is Response.Success -> return

                is Response.Error -> {
                    if (response.errorData.isUnauthorized) {
                        session.clear()
                        return
                    }
                    loadError.value = response.errorData
                }
            }

            // Waits for the next return of the connection: an offline start and a failure while online both end there.
            connectivityRepository.isOnline.dropWhile { isOnline -> isOnline }.first { isOnline -> isOnline }
        }
    }
}