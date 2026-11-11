package org.kimoteru.wallify.core.presentation.errors

import org.jetbrains.compose.resources.StringResource

/**
 * Failure of one action that a screen handles in its own way, by the HTTP status [code]. A screen declares the
 * statuses it knows as an enum of its area (`CollectionApiError`) and passes it to `handleResponse`; every other
 * status gets the common handling. [message] is the snackbar text; `null` means the screen shows the failure itself.
 */
interface ApiError {
    val code: Int
    val message: StringResource?
}

/** The entry of the enum [T] with this HTTP status, or `null` when the screen does not handle it in its own way. */
inline fun <reified T> Int.toApiError(): T? where T : Enum<T>, T : ApiError =
    enumValues<T>().firstOrNull { it.code == this }