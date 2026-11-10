package org.kimoteru.wallify.core.data.session

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.data.cache.CacheDatabaseCleaner
import org.kimoteru.wallify.core.data.download.FileDownloader
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.domain.repository.UserRepository

/**
 * Wipes everything the app saved about the user when the session ends: the tables of the database (photos, users,
 * collections, topics, statistics, lists), the downloaded files and the signed-in user.
 *
 * It follows the stored token, so every way out is covered at once: signing out, an expired session (401) and a token
 * removed by anything else. It also runs at the start of an app that has no session, which is cheap and finishes a
 * wipe that an earlier run was stopped in. At the start it also drops what was not loaded for a long time
 * ([CacheDatabaseCleaner.pruneStale]). The settings of the app (theme, layouts, language) are not user data and
 * stay. Cached images are not here: the image loader belongs to the UI, which clears it when the sign-in screen
 * shows up (`ClearImageCacheEffect`).
 */
class SessionDataCleaner(
    private val session: SessionRepository,
    private val databaseCleaner: CacheDatabaseCleaner,
    private val fileDownloader: FileDownloader,
    private val userRepository: UserRepository,
    private val scope: CoroutineScope
) {

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            attempt { databaseCleaner.pruneStale() }
            session.authToken
                .mapDistinctBy { token -> !token.isNullOrBlank() }
                .collect { isSignedIn ->
                    if (!isSignedIn) clearAll()
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    suspend fun clearAll() {
        userRepository.clearCurrentUser()
        attempt { databaseCleaner.clearAll() }
        attempt { fileDownloader.clear() }
    }

    /** One failing step (a locked file) must not leave the other places with the old user's data. */
    private suspend fun attempt(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // The next sign-out or start without a session wipes it again.
        }
    }
}