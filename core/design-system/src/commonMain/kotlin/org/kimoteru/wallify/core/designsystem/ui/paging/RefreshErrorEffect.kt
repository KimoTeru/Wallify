package org.kimoteru.wallify.core.designsystem.ui.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.paging.compose.LazyPagingItems
import org.kimoteru.wallify.core.common.paging.responseError
import org.kimoteru.wallify.core.common.response.ResponseError

/**
 * How the lists of a screen put a failed refresh over shown items into the snackbar: [onChange] gets the failure of a
 * list (or `null` when it is over), [retries] counts the presses of «Повторить». The screen provides it with
 * [ListRefreshErrors]; without it such a failure shows nothing.
 */
@Immutable
class RefreshErrorHandler(
    val retries: Int = 0,
    val onChange: (list: Long, error: ResponseError?) -> Unit = { _, _ -> },
)

val LocalRefreshErrorHandler = compositionLocalOf { RefreshErrorHandler() }

/**
 * Lets the paged lists in [content] put a failed refresh over shown items into the snackbar with «Повторить»
 * (`code/presentation.md` § Ошибка действия). [retries] is the count of presses from the state of the screen,
 * [onChange] sends the screen's own `ListRefreshError` event. A screen wraps its lists in it.
 */
@Composable
fun ListRefreshErrors(
    retries: Int,
    onChange: (list: Long, error: ResponseError?) -> Unit,
    content: @Composable () -> Unit
) {
    val currentOnChange by rememberUpdatedState(onChange)
    val handler = remember(retries) {
        RefreshErrorHandler(retries) { list, error -> currentOnChange(list, error) }
    }

    CompositionLocalProvider(LocalRefreshErrorHandler provides handler, content = content)
}

/**
 * Reports a failed refresh of a list that already shows items (the saved list stays on screen) to
 * [LocalRefreshErrorHandler] and repeats the list when «Повторить» is pressed. The key of the list is its place in
 * the composition, the same after a rotation. A list without items shows its failure in place instead
 * (`ErrorContentLayout`).
 */
@Composable
fun RefreshErrorEffect(paging: LazyPagingItems<*>) {
    val handler = LocalRefreshErrorHandler.current
    val list = currentCompositeKeyHashCode
    val failure = paging.refreshError?.takeIf { paging.itemCount > 0 }
    val currentOnChange by rememberUpdatedState(handler.onChange)
    var reported by remember { mutableStateOf<Throwable?>(null) }
    var handledRetries by remember { mutableIntStateOf(handler.retries) }

    LaunchedEffect(failure) {
        // A new composition starts without items, so only a failure reported here may be over.
        if (failure != null) {
            currentOnChange(list, failure.responseError)
        } else if (reported != null) {
            currentOnChange(list, null)
        }
        reported = failure
    }

    LaunchedEffect(handler.retries) {
        if (handler.retries == handledRetries) return@LaunchedEffect
        handledRetries = handler.retries
        if (reported != null) paging.retry()
    }
}