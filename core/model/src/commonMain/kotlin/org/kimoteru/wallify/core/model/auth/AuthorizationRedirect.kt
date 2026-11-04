package org.kimoteru.wallify.core.model.auth

/** What the backend sent back to the redirect address after the user left its authorization page. */
sealed interface AuthorizationRedirect {

    /** The user granted access; [code] is exchanged for the access token. */
    data class Granted(val code: String) : AuthorizationRedirect

    /** The user declined or the backend refused; [reason] is the backend's description. */
    data class Denied(val reason: String) : AuthorizationRedirect
}