package org.kimoteru.wallify.core.common.response

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class ResourceTest {
    @Test
    fun flowEmitsLoadingThenSuccess() = runTest {
        val states = flow { emit(1) }.asResource().toList()
        assertEquals(listOf(Resource.Loading, Resource.Success(1)), states)
    }

    @Test
    fun flowFailureBecomesError() = runTest {
        val states = flow<Int> { error("boom") }.asResource().toList()
        assertEquals(Resource.Loading, states.first())
        val error = assertIs<Resource.Error>(states.last())
        assertEquals("boom", error.error.message)
    }

    @Test
    fun successDataSkipsLoadingAndError() = runTest {
        val states = flowOf(Resource.Loading, Resource.Success(1), Resource.Error(ResponseError("nope", 404)))
        assertEquals(listOf(1), states.successData().toList())
    }

    @Test
    fun responseMapsToResource() {
        val error = ResponseError("nope", 404)
        assertEquals(Resource.Error(error), Response.Error(error).asResource())
        assertEquals(Resource.Success("ok"), Response.Success("ok").asResource())
    }

}