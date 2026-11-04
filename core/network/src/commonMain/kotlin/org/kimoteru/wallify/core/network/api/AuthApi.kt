package org.kimoteru.wallify.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.kimoteru.wallify.core.common.extensions.Space
import org.kimoteru.wallify.core.common.extensions.nullIfBlank
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.auth.AuthorizationRedirect
import org.kimoteru.wallify.core.network.config.ApiCredentials
import org.kimoteru.wallify.core.network.config.OAUTH_BASE_URL
import org.kimoteru.wallify.core.network.model.response.auth.TokenResponse
import org.kimoteru.wallify.core.network.response.executeRequest
import org.koin.core.annotation.Singleton

/**
 * User authorization (OAuth, authorization code flow): the user signs in on a backend page in the browser, the
 * backend redirects to the redirect address with a code, the code is exchanged for a token that never expires.
 * The redirect address belongs to the platform that receives it, so every call takes it as an argument.
 * The secret key goes only into the token request.
 */
@Singleton
class AuthApi(
    private val client: HttpClient,
    private val credentials: ApiCredentials
) {

    /** Address of the page where the user signs in and grants every permission in [OAUTH_SCOPES]. */
    fun getAuthorizationUrl(redirectUri: String): String {
        return URLBuilder(OAUTH_BASE_URL + AUTHORIZE_PATH).apply {
            parameters.append(OAuthField.CLIENT_ID, credentials.accessKey)
            parameters.append(OAuthField.REDIRECT_URI, redirectUri)
            parameters.append(OAuthField.RESPONSE_TYPE, RESPONSE_TYPE_CODE)
            parameters.append(OAuthField.SCOPE, OAUTH_SCOPES.joinToString(String.Space))
        }.buildString()
    }

    /**
     * Reads the address the browser was redirected to. Returns `null` when it is not [redirectUri],
     * so it is not an answer of the authorization page.
     */
    fun parseRedirect(url: String, redirectUri: String): AuthorizationRedirect? {
        if (!url.startsWith(redirectUri)) return null

        val parameters = Url(url).parameters
        val code = parameters[OAuthField.CODE]?.nullIfBlank()
        if (code != null) return AuthorizationRedirect.Granted(code = code)

        val reason = parameters[OAuthField.ERROR_DESCRIPTION]?.nullIfBlank()
            ?: parameters[OAuthField.ERROR]?.nullIfBlank()
            ?: DEFAULT_DENIED_REASON
        return AuthorizationRedirect.Denied(reason = reason)
    }

    suspend fun requestToken(code: String, redirectUri: String): Response<TokenResponse> {
        return executeRequest {
            client.post(OAUTH_BASE_URL + TOKEN_PATH) {
                setBody(
                    formContent(
                        OAuthField.CLIENT_ID to credentials.accessKey,
                        OAuthField.CLIENT_SECRET to credentials.secretKey,
                        OAuthField.REDIRECT_URI to redirectUri,
                        OAuthField.CODE to code,
                        OAuthField.GRANT_TYPE to GRANT_TYPE_AUTHORIZATION_CODE
                    )
                )
            }
        }
    }
}

private object OAuthField {
    const val CLIENT_ID = "client_id"
    const val CLIENT_SECRET = "client_secret"
    const val REDIRECT_URI = "redirect_uri"
    const val RESPONSE_TYPE = "response_type"
    const val SCOPE = "scope"
    const val CODE = "code"
    const val GRANT_TYPE = "grant_type"
    const val ERROR = "error"
    const val ERROR_DESCRIPTION = "error_description"
}

private const val AUTHORIZE_PATH = "authorize"
private const val TOKEN_PATH = "token"
private const val RESPONSE_TYPE_CODE = "code"
private const val GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
private const val DEFAULT_DENIED_REASON = "access_denied"

/** Every permission the backend offers; the user sees the list on the authorization page. */
private val OAUTH_SCOPES = listOf(
    "public",
    "read_user",
    "write_user",
    "read_photos",
    "write_photos",
    "read_collections",
    "write_collections"
)