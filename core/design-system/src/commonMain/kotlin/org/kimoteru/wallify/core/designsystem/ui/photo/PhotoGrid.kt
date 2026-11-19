package org.kimoteru.wallify.core.designsystem.ui.photo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import org.kimoteru.wallify.core.common.paging.responseError
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.indicator.DefaultProgressIndicator
import org.kimoteru.wallify.core.designsystem.ui.layout.ContentMessageLayout
import org.kimoteru.wallify.core.designsystem.ui.layout.ErrorContentLayout
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.paging.RefreshErrorEffect
import org.kimoteru.wallify.core.designsystem.ui.paging.isAppending
import org.kimoteru.wallify.core.designsystem.ui.paging.isEmpty
import org.kimoteru.wallify.core.designsystem.ui.paging.isRefreshing
import org.kimoteru.wallify.core.designsystem.ui.paging.keyAt
import org.kimoteru.wallify.core.designsystem.ui.paging.refreshError
import org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh.DefaultPullToRefreshBox
import org.kimoteru.wallify.core.designsystem.ui.shimmer.ShimmerBox

private const val ITEM_CONTENT_TYPE = "item"
private const val PLACEHOLDER_CONTENT_TYPE = "placeholder"

/**
 * Scroll state of a grid that shows one list at a time. It survives a rotation and the screen leaving the stack, and
 * starts again at the top when any of [keys] changes: the list is another one (a topic, a search, a tab, a new set),
 * and the position in the old one means nothing there. Without the keys a new list opens already scrolled.
 */
@Composable
fun rememberPhotoGridState(vararg keys: Any?): LazyStaggeredGridState =
    rememberSaveable(*keys, saver = LazyStaggeredGridState.Saver) { LazyStaggeredGridState() }

/**
 * Grid of photo cards: columns by window width and by [mode], cards separated by the grid spacing. [mode] is the
 * user's choice from the root by default; a screen that always shows equal cards passes [PhotoGridMode.Grid].
 */
@Composable
fun PhotoGridLayout(
    modifier: Modifier = Modifier,
    mode: PhotoGridMode = LocalPhotoGridMode.current,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(),
    content: LazyStaggeredGridScope.() -> Unit
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    val windowType = rememberWindowType()
    val chosenColumns = LocalPhotoGridColumns.current

    val columns = when (mode) {
        PhotoGridMode.List -> when (windowType) {
            WindowType.Compact -> appSize.photoGrid.listColumns
            WindowType.Medium, WindowType.Expanded -> appSize.photoGrid.listWideColumns
        }

        PhotoGridMode.Masonry, PhotoGridMode.Grid -> chosenColumns ?: when (windowType) {
            WindowType.Compact -> appSize.photoGrid.compactColumns
            WindowType.Medium -> appSize.photoGrid.mediumColumns
            WindowType.Expanded -> appSize.photoGrid.expandedColumns
        }
    }

    // The grid is no blur source of its own: the screen marks the content around it (`FloatingBarScreen`), one
    // source per screen.
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        state = state,
        columns = StaggeredGridCells.Fixed(columns),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(appPadding.medium),
        verticalItemSpacing = appSize.photoGrid.spacing,
        content = content
    )
}

/** Placeholder grid shown while the first page loads. */
@Composable
fun PhotoGridShimmer(
    modifier: Modifier = Modifier,
    mode: PhotoGridMode = LocalPhotoGridMode.current,
    contentPadding: PaddingValues = PaddingValues()
) {
    val appCorner = AppTheme.corner
    val appRatio = AppTheme.ratio

    val ratios = listOf(
        appRatio.image.portrait,
        appRatio.image.square,
        appRatio.image.landscape,
        appRatio.image.portrait,
        appRatio.image.landscape,
        appRatio.image.square
    )

    PhotoGridLayout(
        modifier = modifier,
        mode = mode,
        contentPadding = contentPadding
    ) {
        items(count = ratios.size * 2) { index ->
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratios[index % ratios.size]),
                shape = RoundedCornerShape(appCorner.large + appCorner.smallSpecial)
            )
        }
    }
}

/**
 * Photo masonry over a paged list with every state: shimmer while loading, an error with retry on the whole content
 * while there are no photos, an empty message, a loader under the list while the next page loads and pull to
 * refresh, which asks the backend whatever the age of the list. A failed refresh over shown photos goes to
 * [onRefreshError] and is repeated when [refreshRetries] grows ([RefreshErrorEffect]). [itemContent] draws one item,
 * usually a [PhotoCard] with the same [mode]; an item that is not loaded yet is a [PhotoCardPlaceholder].
 */
@Composable
fun <T : Any> PagingPhotoGrid(
    modifier: Modifier = Modifier,
    paging: LazyPagingItems<T>,
    itemKey: (T) -> Any,
    emptyText: String,
    refreshRetries: Int,
    onRefreshError: (list: Long, error: ResponseError?) -> Unit,
    mode: PhotoGridMode = LocalPhotoGridMode.current,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(),
    itemContent: @Composable (T) -> Unit
) {
    val appPadding = AppTheme.padding

    // A list that failed to load offline loads again by itself when the connection returns.
    RetryOnReconnect(
        isFailed = paging.itemCount == 0 && paging.refreshError != null,
        onRetry = { paging.retry() }
    )

    RefreshErrorEffect(
        paging = paging,
        retries = refreshRetries,
        onChange = onRefreshError
    )

    when {
        paging.itemCount == 0 && paging.isRefreshing -> PhotoGridShimmer(
            modifier = modifier,
            mode = mode,
            contentPadding = contentPadding
        )

        paging.itemCount == 0 && paging.refreshError != null -> ErrorContentLayout(
            modifier = modifier,
            error = paging.refreshError?.responseError,
            onRetry = { paging.retry() }
        )

        paging.isEmpty -> ContentMessageLayout(
            modifier = modifier,
            title = emptyText
        )

        else -> DefaultPullToRefreshBox(
            modifier = modifier,
            isRefreshing = paging.isRefreshing,
            onRefresh = { paging.refresh() },
            indicatorPadding = PaddingValues(top = contentPadding.calculateTopPadding())
        ) {
            PhotoGridLayout(
                modifier = Modifier.fillMaxSize(),
                mode = mode,
                state = state,
                contentPadding = contentPadding
            ) {
                // A type per kind of item lets the grid reuse the nodes of a card for another card, not a placeholder.
                items(
                    count = paging.itemCount,
                    key = { index -> paging.keyAt(index) { itemKey(it) } },
                    contentType = { index ->
                        if (paging.peek(index) != null) ITEM_CONTENT_TYPE else PLACEHOLDER_CONTENT_TYPE
                    }
                ) { index ->
                    val item = paging[index]

                    if (item != null) itemContent(item) else PhotoCardPlaceholder(mode = mode)
                }

                if (paging.isAppending) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(appPadding.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            DefaultProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}