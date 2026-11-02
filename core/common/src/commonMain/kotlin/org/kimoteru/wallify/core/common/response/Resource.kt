package org.kimoteru.wallify.core.common.response

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** Loading state wrapper for UI: loading, data or error. */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>

    data class Success<T>(val data: T) : Resource<T>

    data class Error(val error: ResponseError) : Resource<Nothing>
}

fun <T> Response<T>.asResource(): Resource<T> = when (this) {
    is Response.Success -> Resource.Success(data)
    is Response.Error -> Resource.Error(errorData)
}

/** Emits [Resource.Loading], then each value as [Resource.Success]. Failures become [Resource.Error]. */
fun <T> Flow<T>.asResource(): Flow<Resource<T>> = map<T, Resource<T>> { value -> Resource.Success(value) }
    .onStart { emit(Resource.Loading) }
    .catch { throwable ->
        if (throwable is CancellationException) throw throwable
        emit(Resource.Error(ResponseError(message = throwable.message ?: "Unknown error")))
    }

fun <T> Flow<Response<T>>.asResourceFlow(): Flow<Resource<T>> = map { response -> response.asResource() }
    .onStart { emit(Resource.Loading) }