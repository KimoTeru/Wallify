package org.kimoteru.wallify.core.presentation.errors

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.kimoteru.wallify.core.common.response.ResponseError

class ApiFailureTest {

    @Test
    fun toApiErrorFindsTheEntryByCode() {
        assertEquals(CollectionApiError.NOT_FOUND, 404.toApiError<CollectionApiError>())
        assertEquals(AuthApiError.CLIENT_REJECTED, 401.

        toApiError<AuthApiError>())
    }

    @Test
    fun toApiErrorIsNullForAStatusTheEnumDoesNotHandle() {
        assertNull(500.toApiError<CollectionApiError>())
        assertNull(400.toApiError<DefaultApiError>())
    }

    @Test
    fun rateLimitGoesBeforeTheTypedForbidden() {
        val error = ResponseError(message = "Rate Limit Exceeded", code = 403, isRateLimited = true)

        assertEquals(ApiFailure.RateLimited, resolve<CollectionApiError>(error))
    }

    @Test
    fun rateLimitIsNotOfferedForARepeat() {
        val error = ResponseError(message = "Too many requests", code = 429)

        assertEquals(ApiFailure.RateLimited, resolve<DefaultApiError>(error))
    }

    @Test
    fun typedStatusGoesToTheScreen() {
        val error = ResponseError(message = "Forbidden", code = 403)

        assertEquals(ApiFailure.Typed(CollectionApiError.FORBIDDEN), resolve<CollectionApiError>(error))
    }

    @Test
    fun unauthorizedIsLeftToTheDataLayer() {
        val error = ResponseError(message = "Unauthorized", code = 401)

        assertEquals(ApiFailure.SessionExpired, resolve<CollectionApiError>(error))
    }

    @Test
    fun signInHandlesItsOwnUnauthorized() {
        val error = ResponseError(message = "invalid_client", code = 401)

        assertEquals(ApiFailure.Typed(AuthApiError.CLIENT_REJECTED), resolve<AuthApiError>(error))
    }

    @Test
    fun serverFailureIsOfferedForARepeat() {
        val error = ResponseError(message = "Server error", code = 503)

        assertEquals(ApiFailure.Retryable, resolve<DefaultApiError>(error))
    }

    @Test
    fun otherFailuresAreOfferedForARepeat() {
        assertEquals(ApiFailure.Retryable, resolve<DefaultApiError>(ResponseError(message = "No connection")))
        assertEquals(ApiFailure.Retryable, resolve<DefaultApiError>(ResponseError(message = "Gone", code = 410)))
    }

    private inline fun <reified E> resolve(error: ResponseError): ApiFailure<E> where E : Enum<E>, E : ApiError =
        resolveApiFailure(error, error.code.toApiError<E>())
}