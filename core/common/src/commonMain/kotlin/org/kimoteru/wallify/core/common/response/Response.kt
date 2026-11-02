package org.kimoteru.wallify.core.common.response

import kotlinx.coroutines.CancellationException

data class ResponseError(
    val message: String,
    val code: Int = DEBUG_ERROR_CODE
) {
    companion object {
        const val DEBUG_ERROR_CODE = -1
    }
}

sealed class Response<out T> {
    data class Success<T>(val data: T) : Response<T>()
    data class Error(val errorData: ResponseError) : Response<Nothing>()
}

suspend fun <T, R> Response<T>.map(
    successTransform: suspend (T) -> R
): Response<R> = when (this) {
    is Response.Success -> Response.Success(successTransform(data))
    is Response.Error -> this
}

suspend fun <T, R> Response<T>.flatMap(
    transform: suspend (T) -> Response<R>
): Response<R> = when (this) {
    is Response.Success -> transform(data)
    is Response.Error -> this
}

suspend fun <T> Response<T>.onSuccess(
    onSuccess: suspend (T) -> Unit
): Response<T> {
    if (this is Response.Success) onSuccess(data)
    return this
}

suspend fun <T> Response<T>.onError(
    onError: suspend (ResponseError) -> Unit
): Response<T> {
    if (this is Response.Error) onError(errorData)
    return this
}

fun <T> Response<T>.getSuccessOrNull(): T? = when (this) {
    is Response.Success -> data
    is Response.Error -> null
}

fun <T> Response<T>.getOrNull(): T? = getSuccessOrNull()

fun ResponseError.toErrorResponse(): Response.Error = Response.Error(this)

suspend fun <T> Response<T>.handleResponse(
    onSuccess: (suspend (T) -> Unit)? = null,
    onError: (suspend (ResponseError) -> Unit)? = null
) {
    when (this) {
        is Response.Success -> onSuccess?.invoke(data)
        is Response.Error -> onError?.invoke(errorData)
    }
}

/** Runs [block] and wraps its outcome. Cancellation is rethrown. */
suspend inline fun <T> responseOf(block: suspend () -> T): Response<T> = try {
    Response.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (exception: Exception) {
    Response.Error(ResponseError(message = exception.message ?: "Unknown error"))
}

inline fun <A, B, R> combineResponses(
    a: Response<A>,
    b: Response<B>,
    onSuccess: (A, B) -> R
): Response<R> = when {
    a is Response.Success && b is Response.Success -> Response.Success(onSuccess(a.data, b.data))
    a is Response.Error -> a
    b is Response.Error -> b
    else -> Response.Error(ResponseError("Unknown error"))
}

suspend fun <T1, T2, R> combineResponse(
    t1: Response<T1>,
    t2: suspend (T1) -> Response<T2>,
    successTransform: suspend (T1, T2) -> R
): Response<R> = t1.flatMap { d1 ->
    t2(d1).map { d2 -> successTransform(d1, d2) }
}

suspend fun <T1, T2, T3, R> combineResponse(
    t1: Response<T1>,
    t2: suspend (T1) -> Response<T2>,
    t3: suspend (T1, T2) -> Response<T3>,
    successTransform: suspend (T1, T2, T3) -> R
): Response<R> = t1.flatMap { d1 ->
    t2(d1).flatMap { d2 ->
        t3(d1, d2).map { d3 -> successTransform(d1, d2, d3) }
    }
}

suspend fun <T1, T2, T3, T4, R> combineResponse(
    t1: Response<T1>,
    t2: suspend (T1) -> Response<T2>,
    t3: suspend (T1, T2) -> Response<T3>,
    t4: suspend (T1, T2, T3) -> Response<T4>,
    successTransform: suspend (T1, T2, T3, T4) -> R
): Response<R> = t1.flatMap { d1 ->
    t2(d1).flatMap { d2 ->
        t3(d1, d2).flatMap { d3 ->
            t4(d1, d2, d3).map { d4 -> successTransform(d1, d2, d3, d4) }
        }
    }
}

suspend fun <T1, T2, T3, T4, T5, R> combineResponse(
    t1: Response<T1>,
    t2: suspend (T1) -> Response<T2>,
    t3: suspend (T1, T2) -> Response<T3>,
    t4: suspend (T1, T2, T3) -> Response<T4>,
    t5: suspend (T1, T2, T3, T4) -> Response<T5>,
    successTransform: suspend (T1, T2, T3, T4, T5) -> R
): Response<R> = t1.flatMap { d1 ->
    t2(d1).flatMap { d2 ->
        t3(d1, d2).flatMap { d3 ->
            t4(d1, d2, d3).flatMap { d4 ->
                t5(d1, d2, d3, d4).map { d5 -> successTransform(d1, d2, d3, d4, d5) }
            }
        }
    }
}