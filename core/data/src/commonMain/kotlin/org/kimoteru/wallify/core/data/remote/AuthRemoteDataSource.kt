package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.auth.AuthorizationRedirect

interface AuthRemoteDataSource {

    fun getAuthorizationUrl(redirectUri: String): String

    fun parseRedirect(url: String, redirectUri: String): AuthorizationRedirect?

    /** Exchanges the authorization code for the access token. */
    suspend fun requestToken(code: String, redirectUri: String): Response<String>
}