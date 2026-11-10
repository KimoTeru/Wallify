package org.kimoteru.wallify.core.designsystem.ui.photo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.LazyPagingItems
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberWindowType
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurSourceZIndex
import org.kimoteru.wallify.core.designsystem.ui.blur.blurSource
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.indicator.DefaultProgressIndicator
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorDescription
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorTitle
import org.kimoteru.wallify.core.designsystem.ui.paging.isAppending
import org.kimoteru.wallify.core.designsystem.ui.paging.isEmpty
import org.kimoteru.wallify.core.designsystem.ui.paging.isRefreshing
import org.kimoteru.wallify.core.designsystem.ui.paging.keyAt
import org.kimoteru.wallify.core.designsystem.ui.paging.refreshError
import org.kimoteru.wallify.core.designsystem.ui.shimmer.ShimmerBox
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_retry

/**
 * Grid of photo cards: columns by window width and by the chosen [PhotoGridMode], cards separated by the grid
 * spacing.
 */
@Composable
fun PhotoGridLayout(
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(),
    content: LazyStaggeredGridScope.() -> Unit
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    val windowType = rememberWindowType()
    val chosenColumns = LocalPhotoGridColumns.current

    val columns = when (LocalPhotoGridMode.current) {
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

    // The photos are what the frosted bars (dock, search) and the sheets over the screen blur.
    LazyVerticalStaggeredGrid(
        modifier = modifier.blurSource(BlurSourceZIndex.Screen),
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
 * Photo masonry over a paged list with every state: shimmer while loading, an error with retry, an empty message
 * and a loader under the list while the next page loads. [itemContent] draws one item, usually a [PhotoCard].
 */
@Composable
fun <T : Any> PagingPhotoGrid(
    modifier: Modifier = Modifier,
    paging: LazyPagingItems<T>,
    itemKey: (T) -> Any,
    emptyText: String,
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

    when {
        paging.itemCount == 0 && paging.isRefreshing -> PhotoGridShimmer(
            modifier = modifier,
            contentPadding = contentPadding
        )

        paging.itemCount == 0 && paging.refreshError != null -> PhotoGridMessage(
            modifier = modifier,
            title = networkErrorTitle(),
            description = networkErrorDescription(),
            actionText = stringResource(Res.string.common_retry),
            onAction = { paging.retry() }
        )

        paging.isEmpty -> PhotoGridMessage(
            modifier = modifier,
            title = emptyText
        )

        else -> PhotoGridLayout(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding
        ) {
            items(
                count = paging.itemCount,
                key = { index -> paging.keyAt(index) { itemKey(it) } }
            ) { index ->
                paging[index]?.let { itemContent(it) }
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

/** Centered message with an optional action, for the empty and failed states of a photo list. */
@Composable
fun PhotoGridMessage(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appPadding.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appPadding.medium, Alignment.CenterVertically)
    ) {
        Text(
            text = title,
            style = appTypography.h4.semiBold,
            color = appColor.text.title,
            textAlign = TextAlign.Center
        )

        description?.let {
            Text(
                text = it,
                style = appTypography.h6.regular,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )
        }

        if (actionText != null && onAction != null) {
            GlassButton(text = actionText, onClick = onAction)
        }
    }
}