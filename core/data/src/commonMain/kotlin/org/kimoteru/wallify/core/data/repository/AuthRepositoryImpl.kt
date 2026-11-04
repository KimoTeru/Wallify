package org.kimoteru.wallify.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.flow.mapDistinctBy
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.common.response.onSuccess
import org.kimoteru.wallify.core.common.response.responseOf
import org.kimoteru.wallify.core.data.remote.AuthRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.AuthRepository
import org.kimoteru.wallify.core.domain.repository.SessionRepository
import org.kimoteru.wallify.core.model.auth.AuthorizationRedirect
import org.kimoteru.wallify.core.platform.managers.auth.AuthBrowser
import org.koin.core.annotation.Singleton

@Singleton
class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authBrowser: AuthBrowser,
    private val sessionRepository: SessionRepository
) : AuthRepository {

    override val isSignedIn: Flow<Boolean> = sessionRepository.authToken
        .mapDistinctBy { token -> !token.isNullOrBlank() }

    override suspend fun signIn(): Response<Boolean> {
        val redirectUri = authBrowser.redirectUri
        val authorizationUrl = authRemoteDataSource.getAuthorizationUrl(redirectUri = redirectUri)

        val redirectedTo = when (val opened = responseOf { authBrowser.open(authorizationUrl) }) {
            is Response.Success -> opened.data ?: return Response.Success(false)
            is Response.Error -> return opened
        }

        return when (val redirect = authRemoteDataSource.parseRedirect(url = redirectedTo, redirectUri = redirectUri)) {
            is AuthorizationRedirect.Granted -> exchangeCode(code = redirect.code, redirectUri = redirectUri)
            is AuthorizationRedirect.Denied, null -> Response.Success(false)
        }
    }

    override suspend fun signOut() {
        sessionRepository.clear()
    }

    private suspend fun exchangeCode(code: String, redirectUri: String): Response<Boolean> {
        return authRemoteDataSource.requestToken(code = code, redirectUri = redirectUri)
            .onSuccess { token -> sessionRepository.updateAuthToken(token) }
            .map { true }
    }
}