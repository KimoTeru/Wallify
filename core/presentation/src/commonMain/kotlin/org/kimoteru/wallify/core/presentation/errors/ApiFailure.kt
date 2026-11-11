package org.kimoteru.wallify.core.presentation.errors

import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.ResponseErrorType

/** What `handleResponse` shows for a failed action. Nothing is repeated by itself: a repeat is the user's choice. */
sealed interface ApiFailure<out E : ApiError> {
    /** The screen handles this status in its own way. */
    data class Typed<E : ApiError>(val error: E) : ApiFailure<E>

    /** The backend does not accept the session: the data layer has already ended it, nothing to show. */
    data object SessionExpired : ApiFailure<Nothing>

    /** The request limit is exhausted: the text without a repeat, a repeat would only spend the next hour's quota. */
    data object RateLimited : ApiFailure<Nothing>

    /** Any other failure, a failure without a status included: the text by the type of the error and a repeat. */
    data object Retryable : ApiFailure<Nothing>
}

/**
 * Decision on a failed action. The request limit goes first because the backend reports it with 403, which a screen
 * may handle as a missing permission; the typed error goes before 401 because only the sign-in declares 401, where
 * there is no session to end yet.
 */
fun <E : ApiError> resolveApiFailure(
    error: ResponseError,
    typed: E?,
): ApiFailure<E> = when {
    error.type == ResponseErrorType.RateLimited -> ApiFailure.RateLimited
    typed != null -> ApiFailure.Typed(typed)
    error.isUnauthorized -> ApiFailure.SessionExpired
    else -> ApiFailure.Retryable
}