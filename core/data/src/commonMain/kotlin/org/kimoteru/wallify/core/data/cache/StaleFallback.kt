package org.kimoteru.wallify.core.data.cache

import org.kimoteru.wallify.core.common.response.ResponseError

private const val SERVER_ERROR_CODE = 500
private const val FORBIDDEN_CODE = 403
private const val TOO_MANY_REQUESTS_CODE = 429

/**
 * Whether saved data may answer in place of this failure: no connection or a broken answer
 * ([ResponseError.DEBUG_ERROR_CODE]), a backend failure, or the request limit (the backend sends 403 or 429 for it).
 * An expired session (401) never qualifies, the user has to see that one, and neither do 400 and 404: the backend
 * understood the request and refused it, so older data would hide the truth.
 */
internal fun ResponseError.canUseStaleData(): Boolean =
    code == ResponseError.DEBUG_ERROR_CODE ||
        code >= SERVER_ERROR_CODE ||
        code == FORBIDDEN_CODE ||
        code == TOO_MANY_REQUESTS_CODE