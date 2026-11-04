package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.model.auth.AuthorizationRedirect
import org.kimoteru.wallify.core.network.api.AuthApi
import org.koin.core.annotation.Singleton

@Singleton
class AuthRemoteDataSourceImpl(
    private val authApi: AuthApi
) : AuthRemoteDataSource {

    override fun getAuthorizationUrl(redirectUri: String): String {
        return authApi.getAuthorizationUrl(redirectUri = redirectUri)
    }

    override fun parseRedirect(url: String, redirectUri: String): AuthorizationRedirect? {
        return authApi.parseRedirect(url = url, redirectUri = redirectUri)
    }

    override suspend fun requestToken(code: String, redirectUri: String): Response<String> {
        return authApi.requestToken(code = code, redirectUri = redirectUri)
            .map { it.accessToken }
    }
}