package org.kimoteru.wallify.core.common.response

import kotlinx.coroutines.CancellationException

/**
 * Response with a typed error [E] (for example an HTTP code or a backend error enum).
 * [ErrorMessage] carries failures that have no typed error, such as network exceptions.
 */
sealed class DetailedResponse<out T, out E> {
    data class Success<T>(val data: T) : DetailedResponse<T, Nothing>()
    data class Error<E>(val error: E) : DetailedResponse<Nothing, E>()
    data class ErrorMessage(val errorMessage: String) : DetailedResponse<Nothing, Nothing>()
}

suspend fun <T, R, E, F> DetailedResponse<T, E>.map(
    success: suspend (T) -> R,
    error: suspend (E) -> F
): DetailedResponse<R, F> = try {
    when (this) {
        is DetailedResponse.Success -> DetailedResponse.Success(success(data))
        is DetailedResponse.Error -> DetailedResponse.Error(error(this.error))
        is DetailedResponse.ErrorMessage -> this
    }
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (exception: Exception) {
    DetailedResponse.ErrorMessage(exception.message.toString())
}

suspend fun <T, R, E> DetailedResponse<T, E>.map(
    successTransform: suspend (T) -> R
): DetailedResponse<R, E> = try {
    when (this) {
        is DetailedResponse.Success -> DetailedResponse.Success(successTransform(data))
        is DetailedResponse.Error -> this
        is DetailedResponse.ErrorMessage -> this
    }
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (exception: Exception) {
    DetailedResponse.ErrorMessage(exception.message.toString())
}

suspend fun <T, E> DetailedResponse<T, E>.onSuccess(
    onSuccess: suspend (T) -> Unit
): DetailedResponse<T, E> {
    if (this is DetailedResponse.Success) onSuccess(data)
    return this
}

suspend fun <T, E> DetailedResponse<T, E>.onError(
    onError: suspend (E) -> Unit
): DetailedResponse<T, E> {
    if (this is DetailedResponse.Error) onError(error)
    return this
}

suspend fun <T, E> DetailedResponse<T, E>.handleDetailedResponse(
    onSuccess: (suspend (T) -> Unit)? = null,
    onError: (suspend (E) -> Unit)? = null,
    onErrorMessage: (suspend (String) -> Unit)? = null,
    errorMessageReplacer: E? = null
) {
    when (this) {
        is DetailedResponse.Success -> onSuccess?.invoke(data)
        is DetailedResponse.Error -> onError?.invoke(error)
        is DetailedResponse.ErrorMessage -> {
            onErrorMessage?.invoke(errorMessage) ?: onError?.invoke(errorMessageReplacer ?: return)
        }
    }
}