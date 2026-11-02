package org.kimoteru.wallify.core.designsystem.ui.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.paging.compose.LazyPagingItems

/** An extra item (banner, ad, header) shown at [globalIndex] inside a paged list. */
@Immutable
data class PagingListInsert(
    val globalIndex: Int,
    val key: Any = -1,
    val content: @Composable LazyItemScope.() -> Unit
)

/**
 * Adds all [paging] items to a `LazyColumn` or `LazyRow`, with an optional [insert] placed at its index.
 * [itemKey] must return a stable unique key for the paging item at the given index, see [keyAt].
 */
fun <T : Any> LazyListScope.pagingItemsWithInsert(
    paging: LazyPagingItems<T>,
    insert: PagingListInsert? = null,
    itemKey: (index: Int) -> Any,
    itemContent: @Composable LazyItemScope.(index: Int) -> Unit
) {
    if (insert == null) {
        items(
            count = paging.itemCount,
            key = itemKey
        ) { index ->
            itemContent(index)
        }
        return
    }

    val at = insert.globalIndex.coerceIn(0, paging.itemCount)

    items(
        count = at,
        key = { index -> itemKey(index) }
    ) { index ->
        itemContent(index)
    }

    item(key = insert.key) {
        insert.content(this)
    }

    items(
        count = paging.itemCount - at,
        key = { offset -> itemKey(at + offset) }
    ) { offset ->
        itemContent(at + offset)
    }
}