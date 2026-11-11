package org.kimoteru.wallify.core.presentation.errors

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_collection_forbidden
import org.kimoteru.wallify.core.resources.generated.resources.error_collection_not_found

/**
 * Failures of changing a collection (adding or removing a photo), by the statuses of the backend's error table:
 * 403 — no permission for the request (a collection of another user), 404 — the resource does not exist.
 */
enum class CollectionApiError(
    override val code: Int,
    override val message: StringResource? = null,
) : ApiError {
    FORBIDDEN(403, Res.string.error_collection_forbidden),
    NOT_FOUND(404, Res.string.error_collection_not_found),
}