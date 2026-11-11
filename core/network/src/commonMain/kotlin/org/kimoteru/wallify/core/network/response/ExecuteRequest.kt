package org.kimoteru.wallify.core.network.response

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import org.kimoteru.wallify.core.common.extensions.nullIfBlank
import org.kimoteru.wallify.core.common.extensions.SemicolonSpace
import org.kimoteru.wallify.core.common.response.DetailedResponse
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.network.json.networkJson
import org.kimoteru.wallify.core.network.model.ApiErrorBody

/**
 * Runs [request] and maps the HTTP result to [Response].
 * Non-2xx responses become [Response.Error] with the HTTP status as `code` and the backend message
 * (see [ApiErrorBody] for the supported error shapes); a 403 that means the request limit gets
 * [ResponseError.isRateLimited]. Transport and decoding failures become [Response.Error]
 * without a code.
 */
suspend inline fun <reified T> executeRequest(
    crossinline request: suspend () -> HttpResponse
): Response<T> = try {
    request().toResponse<T>()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (exception: Exception) {
    Response.Error(
        ResponseError(message = exception.message?.nullIfBlank() ?: "Request failed")
    )
}

/** Same as [executeRequest], but the error is the HTTP status code, see [DetailedResponse]. */
suspend inline fun <reified T> executeRequestWithErrorCode(
    crossinline request: suspend () -> HttpResponse
): DetailedResponse<T, Int> = try {
    val response = request()
    if (response.status.isSuccess()) {
        DetailedResponse.Success(response.bodyOrUnit<T>())
    } else {
        DetailedResponse.Error(response.status.value)
    }
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (exception: Exception) {
    DetailedResponse.ErrorMessage(exception.message.toString())
}

suspend inline fun <reified T> HttpResponse.toResponse(): Response<T> {
    if (status.isSuccess()) {
        return Response.Success(bodyOrUnit<T>())
    }

    val raw = runCatching { bodyAsText() }.getOrNull()?.nullIfBlank()
    val parsed = raw?.let(::decodeErrorBody)
    val message = parsed?.errors?.takeIf { it.isNotEmpty() }?.joinToString(String.SemicolonSpace)
        ?: parsed?.message?.nullIfBlank()
        ?: parsed?.errorDescription?.nullIfBlank()
        ?: parsed?.error?.nullIfBlank()
        ?: raw
        ?: status.description
    return Response.Error(
        ResponseError(message = message, code = status.value, isRateLimited = isRateLimited(message))
    )
}

/**
 * Whether this failure is the exhausted request limit. The backend reports it as 403, the same status as a missing
 * permission, and tells them apart by the remaining-requests header and the error text.
 */
@PublishedApi
internal fun HttpResponse.isRateLimited(message: String): Boolean =
    status == HttpStatusCode.Forbidden && (
        headers[RATE_LIMIT_REMAINING_HEADER] == RATE_LIMIT_EXHAUSTED ||
            message.contains(RATE_LIMIT_EXCEEDED_MESSAGE, ignoreCase = true)
        )

private const val RATE_LIMIT_REMAINING_HEADER = "X-Ratelimit-Remaining"
private const val RATE_LIMIT_EXHAUSTED = "0"
private const val RATE_LIMIT_EXCEEDED_MESSAGE = "Rate Limit Exceeded"

@PublishedApi
internal suspend inline fun <reified T> HttpResponse.bodyOrUnit(): T {
    if (T::class == Unit::class) {
        @Suppress("UNCHECKED_CAST")
        return Unit as T
    }
    return body<T>()
}

@PublishedApi
internal fun decodeErrorBody(raw: String): ApiErrorBody? = try {
    networkJson.decodeFromString<ApiErrorBody>(raw)
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}