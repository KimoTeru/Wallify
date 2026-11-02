package org.kimoteru.wallify.core.network.model

import kotlinx.serialization.Serializable

/**
 * Error payload. Supports `{"errors": ["..."]}`, `{"message": "..."}` and `{"error": "..."}`.
 */
@Serializable
data class ApiErrorBody(
    val errors: List<String>? = null,
    val message: String? = null,
    val error: String? = null,
    val code: Int? = null
)