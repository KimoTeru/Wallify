package org.kimoteru.wallify.core.presentation.errors

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_profile_forbidden
import org.kimoteru.wallify.core.resources.generated.resources.error_profile_invalid

/**
 * Failures of saving the own profile: 400 — the request was not accepted, 422 — fields were not accepted (the
 * backend lists them in `errors`), 403 — the session lacks the right to change the user.
 */
enum class ProfileApiError(
    override val code: Int,
    override val message: StringResource? = null,
) : ApiError {
    BAD_REQUEST(400, Res.string.error_profile_invalid),
    FORBIDDEN(403, Res.string.error_profile_forbidden),
    INVALID_FIELDS(422, Res.string.error_profile_invalid),
}