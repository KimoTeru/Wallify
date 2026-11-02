package org.kimoteru.wallify.core.network.error

interface ApiError {
    val code: Int
    val message: String?
}

inline fun <reified T> Int.toApiError(): T? where T : Enum<T>, T : ApiError =
    enumValues<T>().firstOrNull { it.code == this }

enum class DefaultApiError(
    override val code: Int,
    override val message: String? = null
) : ApiError {
    UNAUTHORIZED(401, "Unauthorized"),
    BACKEND(500, "Server error")
}