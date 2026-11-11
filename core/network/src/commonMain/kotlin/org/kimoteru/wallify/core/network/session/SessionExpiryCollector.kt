package org.kimoteru.wallify.core.network.session

import kotlinx.coroutines.flow.Flow

/**
 * The backend rejected the token of the user: a request sent with `Authorization: Bearer <token>` was answered with
 * 401. The client calls [onRejected] on the thread of the request, so it only queues the header the request went
 * with; the data layer reads [rejectedAuthorizations] and ends the session. Requests without the user's token (the
 * anonymous client, the exchange of the sign-in code for a token) never get here.
 */
interface SessionExpiryCollector {
    val rejectedAuthorizations: Flow<String>

    fun onRejected(authorization: String)
}