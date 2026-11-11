package org.kimoteru.wallify.core.domain.manager

import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.common.response.ResponseError

/**
 * Keeps the data of the signed-in user in step with the session; it works on its own, nobody has to start it.
 * What it cannot hide from the user is exposed here for the root graph to show.
 */
interface UserSessionManager {

    /**
     * Why the signed-in user could not be loaded; `null` when the load went well or is not needed.
     * A token the backend rejects is not here: that ends the session instead. While the failure lasts, the manager
     * retries each time the connection returns or [retryLoad] is called, and the value goes back to `null` on success.
     */
    val loadError: StateFlow<ResponseError?>

    /** Loads the signed-in user again now (the user pressed «Повторить»); does nothing while there is no failure. */
    fun retryLoad()
}