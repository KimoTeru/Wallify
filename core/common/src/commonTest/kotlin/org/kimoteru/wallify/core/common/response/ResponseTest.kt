package org.kimoteru.wallify.core.common.response

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class ResponseTest {
    @Test
    fun mapTransformsSuccess() = runTest {
        val mapped = Response.Success(2).map { it * 3 }
        assertEquals(6, mapped.getOrNull())
    }

    @Test
    fun mapKeepsError() = runTest {
        val error = Response.Error(ResponseError("boom", 500))
        assertNull(error.map { "unused" }.getOrNull())
    }

    @Test
    fun combineResponseChainsSteps() = runTest {
        val result = combineResponse(
            t1 = Response.Success(1),
            t2 = { Response.Success(it + 1) },
            successTransform = { a, b -> a + b }
        )
        assertEquals(3, result.getOrNull())
    }

    @Test
    fun combineResponsesReturnsFirstError() {
        val error: Response<Int> = Response.Error(ResponseError("first", 404))
        val result = combineResponses(error, Response.Success(1)) { a, b -> a + b }
        assertIs<Response.Error>(result)
        assertEquals(404, result.errorData.code)
    }

    @Test
    fun errorTypeFollowsHttpStatus() {
        val expected = mapOf(
            ResponseError.DEBUG_ERROR_CODE to ResponseErrorType.Network,
            400 to ResponseErrorType.BadRequest,
            401 to ResponseErrorType.Unauthorized,
            403 to ResponseErrorType.Forbidden,
            404 to ResponseErrorType.NotFound,
            429 to ResponseErrorType.RateLimited,
            500 to ResponseErrorType.Server,
            503 to ResponseErrorType.Server,
            409 to ResponseErrorType.Unknown
        )
        expected.forEach { (code, type) ->
            assertEquals(type, ResponseError("failed", code).type, "code $code")
        }
    }

    @Test
    fun rateLimitedForbiddenIsRateLimited() {
        val error = ResponseError("Rate Limit Exceeded", 403, isRateLimited = true)
        assertEquals(ResponseErrorType.RateLimited, error.type)
        assertEquals(false, error.isUnauthorized)
    }

    @Test
    fun detailedMapKeepsTypedError() = runTest {
        val detailed: DetailedResponse<Int, Int> = DetailedResponse.Error(401)
        val mapped = detailed.map { it.toString() }
        assertEquals(DetailedResponse.Error(401), mapped)
    }
}