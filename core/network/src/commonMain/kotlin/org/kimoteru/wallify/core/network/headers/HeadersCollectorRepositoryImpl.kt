package org.kimoteru.wallify.core.network.headers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Singleton

@Singleton
class HeadersCollectorRepositoryImpl : HeadersCollectorRepository {
    private val headersState = MutableStateFlow(HeadersRequest())
    override val headersFlow: StateFlow<HeadersRequest> = headersState.asStateFlow()

    override fun setHeaders(headers: HeadersRequest) {
        headersState.value = headers
    }

    override fun updateAuthorization(authorization: String?) {
        headersState.update { current -> current.copy(authorization = authorization) }
    }

    override fun updateLanguageTag(languageTag: String?) {
        headersState.update { current -> current.copy(languageTag = languageTag) }
    }
}