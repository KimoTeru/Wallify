package org.kimoteru.wallify.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Error payload. Supports `{"errors": ["..."]}`, `{"message": "..."}`, `{"error": "..."}` and the authorization
 * form `{"error": "invalid_grant", "error_description": "..."}`.
 */
@Serializable
data class ApiErrorBody(
    val errors: List<String>? = null,
    val message: String? = null,
    val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    val code: Int? = null
)