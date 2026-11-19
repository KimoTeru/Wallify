package org.kimoteru.wallify.core.data.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.domain.repository.ConnectivityRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepository

/**
 * The loading of the signed-in user that `UserRepositoryImpl` owns: a separate class only so it is tested without the
 * database. [refresh] loads the user into the repository, [clear] removes it.
 *
 * It follows the `Authorization` of [HeadersCollectorRepository], not the stored token: the user is requested only
 * once the HTTP client really sends the new token, otherwise the request would go out with the old one and fail.
 * A new token loads the user, a missing token clears it once the storage has none either (a sign-out).
 * Any other failure goes to [loadError] and the load is repeated when the connection returns after a drop or the user
 * asks for it ([retry]). A 401 is not a failure here: `SessionExpiryHandler` ends the session.
 */
internal class CurrentUserSync(
    private val headersCollector: HeadersCollectorRepository,
    private val session: SessionRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val refresh: suspend () -> Response<User>,
    private val clear: () -> Unit
) {

    val loadError: StateFlow<ResponseError?>
        field = MutableStateFlow<ResponseError?>(null)

    /** Repeats the user asked for; conflated: several presses while the load waits are one more load. */
    private val retryRequests = Channel<Unit>(Channel.CONFLATED)

    fun start(scope: CoroutineScope) {
        scope.launch {
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

    fun retry() {
        if (loadError.value != null) retryRequests.trySend(Unit)
    }

    /**
     * The client starts without a token and gets it from the storage a moment later, so "no token" alone does not
     * mean a sign-out. Only when the storage holds no token either is the session over; otherwise the saved user stays,
     * and the app opens with it even without a connection.
     */
    private suspend fun clearUserIfSignedOut() {
        if (session.authToken.first().isNullOrBlank()) clear()
    }

    private suspend fun loadUser() {
        while (true) {
            loadError.value = null

            when (val response = refresh()) {
                is Response.Success -> return

                is Response.Error -> {
                    // The client has already reported the rejected token, and the session is ending: nothing to show.
                    if (response.errorData.isUnauthorized) return
                    loadError.value = response.errorData
                }
            }

            // A repeat asked for before this failure is not an answer to it.
            retryRequests.tryReceive()

            // Waits for the next return of the connection (an offline start and a failure while online both end
            // there) or for the user's repeat.
            merge(
                connectivityRepository.isOnline
                    .dropWhile { isOnline -> isOnline }
                    .filter { isOnline -> isOnline }
                    .map { },
                retryRequests.receiveAsFlow()
            ).first()
        }
    }
}