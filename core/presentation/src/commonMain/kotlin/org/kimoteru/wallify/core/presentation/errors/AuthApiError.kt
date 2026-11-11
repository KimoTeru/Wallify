package org.kimoteru.wallify.core.presentation.errors

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_auth_client_rejected
import org.kimoteru.wallify.core.resources.generated.resources.error_auth_code_rejected

/**
 * Failures of exchanging the authorization code for a token, by the OAuth 2 token errors (RFC 6749 § 5.2):
 * 400 — the code was not accepted (`invalid_grant`), 401 — the app was not accepted (`invalid_client`). The
 * sign-in has no session yet, so here 401 is not an expired session.
 */
enum class AuthApiError(
    override val code: Int,
    override val message: StringResource? = null,
) : ApiError {
    CODE_REJECTED(400, Res.string.error_auth_code_rejected),
    CLIENT_REJECTED(401, Res.string.error_auth_client_rejected),
}