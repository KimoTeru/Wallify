package org.kimoteru.wallify.core.common.response

/**
 * A [Response.Error] where a result can only fail by throwing: a page of a paged list (`LoadState.Error`). [error]
 * keeps the HTTP status and the [ResponseErrorType] for the screen. A flow of saved data does not throw: it emits
 * [Resource.Error].
 */
class ResponseException(
    val error: ResponseError,
) : Exception(error.message) {
    /** HTTP status or [ResponseError.DEBUG_ERROR_CODE]. */
    val code: Int
        get() = error.code

    val type: ResponseErrorType
        get() = error.type
}

/**
 * Error behind a failure: the [ResponseError] of a [ResponseException], otherwise (a thrown exception) an error
 * without an HTTP status.
 */
val Throwable.responseError: ResponseError
    get() = (this as? ResponseException)?.error ?: ResponseError(message = message ?: "Unknown error")