package org.kimoteru.wallify.core.data.cache

import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.ResponseErrorType

/**
 * Whether saved data may answer in place of this failure: no connection or a broken answer
 * ([ResponseErrorType.Network]), a backend failure, or the request limit ([ResponseErrorType.RateLimited], and any
 * other 403, since the backend may send the limit as a plain 403).
 * An expired session (401) never qualifies, the user has to see that one, and neither do 400 and 404: the backend
 * understood the request and refused it, so older data would hide the truth.
 */
internal fun ResponseError.canUseStaleData(): Boolean = when (type) {
    ResponseErrorType.Network,
    ResponseErrorType.Server,
    ResponseErrorType.RateLimited,
    ResponseErrorType.Forbidden -> true
    ResponseErrorType.BadRequest,
    ResponseErrorType.Unauthorized,
    ResponseErrorType.NotFound,
    ResponseErrorType.Unknown -> false
}