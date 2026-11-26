package org.kimoteru.wallify.core.designsystem.ui.paging

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/**
 * Scrolls [state] to the top when a refresh of [paging] finishes successfully while the list is scrolled: the refresh
 * replaces the head of the saved list, and the user would stay at the same index under new photos. Only the refresh of
 * the backend ([androidx.paging.CombinedLoadStates.mediator]) counts, so the list needs a `RemoteMediator`; without
 * one nothing happens. A failure, an append and the first show of the saved list do not scroll.
 */
@Composable
fun ScrollToTopOnRefreshEffect(
    paging: LazyPagingItems<*>,
    state: LazyStaggeredGridState
) {
    // Only the mediator: a new LazyPagingItems starts with `refresh = Loading` and no mediator, and the replay of the
    // saved list would look like a finished refresh.
    val refresh = paging.loadState.mediator?.refresh
    var wasLoading by remember(paging) { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        if (refresh is LoadState.Loading) {
            wasLoading = true
        } else {
            if (refresh is LoadState.NotLoading && wasLoading && state.canScrollBackward) {
                state.scrollToItem(0)
            }
            wasLoading = false
        }
    }
}