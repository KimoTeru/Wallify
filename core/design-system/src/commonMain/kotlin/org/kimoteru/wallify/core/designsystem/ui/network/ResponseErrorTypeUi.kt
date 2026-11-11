package org.kimoteru.wallify.core.designsystem.ui.network

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.common.response.ResponseErrorType
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_bad_request_description
import org.kimoteru.wallify.core.resources.generated.resources.error_bad_request_title
import org.kimoteru.wallify.core.resources.generated.resources.error_dialog_description
import org.kimoteru.wallify.core.resources.generated.resources.error_dialog_title
import org.kimoteru.wallify.core.resources.generated.resources.error_forbidden_description
import org.kimoteru.wallify.core.resources.generated.resources.error_forbidden_title
import org.kimoteru.wallify.core.resources.generated.resources.error_not_found_description
import org.kimoteru.wallify.core.resources.generated.resources.error_not_found_title
import org.kimoteru.wallify.core.resources.generated.resources.error_rate_limited_description
import org.kimoteru.wallify.core.resources.generated.resources.error_rate_limited_title
import org.kimoteru.wallify.core.resources.generated.resources.error_server_description
import org.kimoteru.wallify.core.resources.generated.resources.error_server_title
import org.kimoteru.wallify.core.resources.generated.resources.error_unauthorized_description
import org.kimoteru.wallify.core.resources.generated.resources.error_unauthorized_title
import org.kimoteru.wallify.core.resources.generated.resources.error_unknown_description

/**
 * Title and description of an error state of a type. [description] is also the text of a failure snackbar; when
 * [hasCode], it takes the HTTP status code as its only argument.
 */
enum class ResponseErrorTypeUi(
    internal val domain: ResponseErrorType,
    val title: StringResource,
    val description: StringResource,
    val hasCode: Boolean = true,
) {
    Network(
        domain = ResponseErrorType.Network,
        title = Res.string.error_dialog_title,
        description = Res.string.error_dialog_description,
        hasCode = false
    ),
    BadRequest(
        domain = ResponseErrorType.BadRequest,
        title = Res.string.error_bad_request_title,
        description = Res.string.error_bad_request_description
    ),
    Unauthorized(
        domain = ResponseErrorType.Unauthorized,
        title = Res.string.error_unauthorized_title,
        description = Res.string.error_unauthorized_description
    ),
    Forbidden(
        domain = ResponseErrorType.Forbidden,
        title = Res.string.error_forbidden_title,
        description = Res.string.error_forbidden_description
    ),
    RateLimited(
        domain = ResponseErrorType.RateLimited,
        title = Res.string.error_rate_limited_title,
        description = Res.string.error_rate_limited_description
    ),
    NotFound(
        domain = ResponseErrorType.NotFound,
        title = Res.string.error_not_found_title,
        description = Res.string.error_not_found_description
    ),
    Server(
        domain = ResponseErrorType.Server,
        title = Res.string.error_server_title,
        description = Res.string.error_server_description
    ),
    Unknown(
        domain = ResponseErrorType.Unknown,
        title = Res.string.error_dialog_title,
        description = Res.string.error_unknown_description
    ),
}

fun ResponseErrorTypeUi.toDomain(): ResponseErrorType = domain

fun ResponseErrorType.toPresentation(): ResponseErrorTypeUi = ResponseErrorTypeUi.entries.first { it.domain == this }