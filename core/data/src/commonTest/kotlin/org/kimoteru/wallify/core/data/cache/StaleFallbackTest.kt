package org.kimoteru.wallify.core.data.cache

import org.kimoteru.wallify.core.common.response.ResponseError
import kotlin.test.Test
import kotlin.test.assertEquals

class StaleFallbackTest {

    @Test
    fun `no connection, backend failure and request limit allow saved data`() {
        listOf(ResponseError.DEBUG_ERROR_CODE, 500, 503, 403, 429).forEach { code ->
            assertEquals(true, error(code).canUseStaleData(), "code $code")
        }
    }

    @Test
    fun `request limit reported by the network layer allows saved data`() {
        val limited = ResponseError(message = "Rate Limit Exceeded", code = 403, isRateLimited = true)

        assertEquals(true, limited.canUseStaleData())
    }

    @Test
    fun `expired session and refused requests never allow saved data`() {
        listOf(ResponseError.UNAUTHORIZED_CODE, 400, 404).forEach { code ->
            assertEquals(false, error(code).canUseStaleData(), "code $code")
        }
    }

    private fun error(code: Int) = ResponseError(message = "failed", code = code)
}