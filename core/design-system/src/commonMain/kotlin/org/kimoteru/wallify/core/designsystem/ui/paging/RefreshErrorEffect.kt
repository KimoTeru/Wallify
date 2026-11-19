package org.kimoteru.wallify.core.designsystem.ui.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * Reports a failed refresh of a list that already shows items (the saved list stays on screen) to [onChange] and
 * repeats the list when [retries] grows (the presses of «Повторить» from the state of the screen,
 * `code/presentation.md` § Ошибка действия). [onChange] gets the key of the list and its failure, or `null` when
 * the failure is over; the screen sends its own `ListRefreshError` event from it. The key of the list is its place
 * in the composition, the same after a rotation. A list without items shows its failure in place instead
 * (`ErrorContentLayout`).
 */
@Composable
fun RefreshErrorEffect(
    paging: LazyPagingItems<*>,
    retries: Int,
    onChange: (list: Long, error: ResponseError?) -> Unit,
) {
    val list = currentCompositeKeyHashCode
    val failure = paging.refreshError?.takeIf { paging.itemCount > 0 }
    val currentOnChange by rememberUpdatedState(onChange)
    var reported by remember { mutableStateOf<Throwable?>(null) }
    var handledRetries by remember { mutableIntStateOf(retries) }

    LaunchedEffect(failure) {
        // A new composition starts without items, so only a failure reported here may be over.
        if (failure != null) {
            currentOnChange(list, failure.responseError)
        } else if (reported != null) {
            currentOnChange(list, null)
        }
        reported = failure
    }

    LaunchedEffect(retries) {
        if (retries == handledRetries) return@LaunchedEffect
        handledRetries = retries
        if (reported != null) paging.retry()
    }
}