package org.kimoteru.wallify.core.common.response

/**
 * Kind of a failed data operation, independent of the backend. The screen picks its text by it; the HTTP status
 * stays in [ResponseError.code].
 */
enum class ResponseErrorType {
    /** No HTTP status ([ResponseError.DEBUG_ERROR_CODE]): no connection, timeout or an unreadable answer. */
    Network,

    /** 400: the backend did not accept the request parameters. */
    BadRequest,

    /** 401: the session token (or its absence) is not accepted, the user has to sign in again. */
    Unauthorized,

    /** 403 that is not the request limit: the user has no rights for this action. */
    Forbidden,

    /** The request limit is exhausted (429, or a backend-specific 403 recognized by the network layer). */
    RateLimited,

    /** 404: the requested item does not exist. */
    NotFound,

    /** 5xx: the backend failed. */
    Server,

    /** Any other status. */
    Unknown;

    companion object {
        private const val BAD_REQUEST_CODE = 400
        private const val FORBIDDEN_CODE = 403
        private const val NOT_FOUND_CODE = 404
        private const val TOO_MANY_REQUESTS_CODE = 429
        private const val SERVER_ERROR_CODE = 500

        /** Type of an error with the HTTP status [code]; [isRateLimited] marks a 403 that means the request limit. */
        fun of(
            code: Int,
            isRateLimited: Boolean = false,
        ): ResponseErrorType = when {
            code == ResponseError.DEBUG_ERROR_CODE -> Network
            isRateLimited || code == TOO_MANY_REQUESTS_CODE -> RateLimited
            code == BAD_REQUEST_CODE -> BadRequest
            code == ResponseError.UNAUTHORIZED_CODE -> Unauthorized
            code == FORBIDDEN_CODE -> Forbidden
            code == NOT_FOUND_CODE -> NotFound
            code >= SERVER_ERROR_CODE -> Server
            else -> Unknown
        }
    }
}