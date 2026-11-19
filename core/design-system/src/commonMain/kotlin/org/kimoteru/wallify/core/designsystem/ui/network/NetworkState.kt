package org.kimoteru.wallify.core.designsystem.ui.network

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.ResponseErrorType

/** Title of an error state by the type of [error]; no [error] or no HTTP status is a network failure. */
@Composable
fun networkErrorTitle(error: ResponseError? = null): String =
    stringResource((error?.type ?: ResponseErrorType.Network).toPresentation().title)

/** Description of an error state by the type of [error], with its HTTP status code. */
@Composable
fun networkErrorDescription(error: ResponseError? = null): String {
    val type = (error?.type ?: ResponseErrorType.Network).toPresentation()

    return when {
        type.hasCode -> stringResource(type.description, error?.code.toString())
        else -> stringResource(type.description)
    }
}