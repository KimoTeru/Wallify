package org.kimoteru.wallify.core.presentation.errors

import org.jetbrains.compose.resources.StringResource

/**
 * No status is handled in its own way: `handleResponseDefault` gives every failure the common handling (session,
 * request limit, text by the type of the error with a repeat). Empty on purpose: a general status here would take
 * the failure away from that handling.
 */
enum class DefaultApiError(
    override val code: Int,
    override val message: StringResource? = null,
) : ApiError