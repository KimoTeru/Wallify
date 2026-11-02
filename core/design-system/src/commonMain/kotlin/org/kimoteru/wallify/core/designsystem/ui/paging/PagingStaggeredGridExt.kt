package org.kimoteru.wallify.core.designsystem.ui.paging

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.paging.compose.LazyPagingItems

/** An extra item (banner, ad, header) shown at [globalIndex] inside a paged staggered grid. */
@Immutable
data class PagingStaggeredInsert(
    val globalIndex: Int,
    val key: Any = -1,
    val span: StaggeredGridItemSpan,
    val content: @Composable LazyStaggeredGridItemScope.() -> Unit
)

/**
 * Adds all [paging] items to a `LazyVerticalStaggeredGrid`, with an optional [insert] placed at its index.
 * [itemKey] must return a stable unique key for the paging item at the given index, see [keyAt].
 */
fun <T : Any> LazyStaggeredGridScope.pagingItemsWithInsert(
    paging: LazyPagingItems<T>,
    insert: PagingStaggeredInsert? = null,
    itemKey: (index: Int) -> Any,
    itemSpan: (index: Int) -> StaggeredGridItemSpan = { StaggeredGridItemSpan.SingleLane },
    itemContent: @Composable LazyStaggeredGridItemScope.(index: Int) -> Unit
) {
    if (insert == null) {
        items(
            count = paging.itemCount,
            key = itemKey,
            span = { index -> itemSpan(index) }
        ) { index ->
            itemContent(index)
        }
        return
    }

    val at = insert.globalIndex.coerceIn(0, paging.itemCount)

    items(
        count = at,
        key = { index -> itemKey(index) },
        span = { index -> itemSpan(index) }
    ) { index ->
        itemContent(index)
    }

    item(
        key = insert.key,
        span = insert.span
    ) {
        insert.content(this)
    }

    items(
        count = paging.itemCount - at,
        key = { offset -> itemKey(at + offset) },
        span = { offset -> itemSpan(at + offset) }
    ) { offset ->
        itemContent(at + offset)
    }
}