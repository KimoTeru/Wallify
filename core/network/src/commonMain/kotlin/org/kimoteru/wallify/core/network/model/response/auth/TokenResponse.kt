package org.kimoteru.wallify.core.network.model.response.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Answer of the token endpoint. The access token does not expire. */
@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("token_type")
    val tokenType: String? = null,

    @SerialName("scope")
    val scope: String? = null,

    @SerialName("created_at")
    val createdAt: Long? = null
)