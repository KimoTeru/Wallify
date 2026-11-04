package org.kimoteru.wallify.core.platform.managers.auth

/**
 * Signs the user in through the system browser: opens the authorization page and waits until the browser
 * is redirected back to the app. The redirect address differs per platform and must be listed in the redirect URIs
 * of the application on the backend (`docs/setup/config-and-secrets.md`).
 */
interface AuthBrowser {

    /** Address the browser is redirected to after the user grants or declines access. */
    val redirectUri: String

    /**
     * Opens [authorizationUrl] in the browser and suspends until the redirect arrives.
     * Returns the full redirect address, or `null` when the user did not come back in time.
     * Cancelling the caller stops the waiting. Throws when the browser cannot be opened.
     */
    suspend fun open(authorizationUrl: String): String?
}