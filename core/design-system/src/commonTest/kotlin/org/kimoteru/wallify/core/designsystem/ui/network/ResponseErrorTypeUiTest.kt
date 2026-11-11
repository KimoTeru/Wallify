package org.kimoteru.wallify.core.designsystem.ui.network

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kimoteru.wallify.core.common.response.ResponseErrorType

/**
 * Every error type has its own texts and back. The mapper finds the value by `domain` instead of a `when`, so a type
 * missing in [ResponseErrorTypeUi] fails here, not on the screen.
 */
class ResponseErrorTypeUiTest {

    @Test
    fun everyErrorTypeMapsBothWays() {
        ResponseErrorType.entries.forEach { type -> assertEquals(type, type.toPresentation().toDomain()) }
        ResponseErrorTypeUi.entries.forEach { type -> assertEquals(type, type.toDomain().toPresentation()) }
    }
}