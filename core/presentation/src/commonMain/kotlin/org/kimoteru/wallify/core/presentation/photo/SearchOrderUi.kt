package org.kimoteru.wallify.core.presentation.photo

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.model.photo.SearchOrder
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.order_latest
import org.kimoteru.wallify.core.resources.generated.resources.order_relevant

/** Label of a search order. */
enum class SearchOrderUi(
    internal val domain: SearchOrder,
    val label: StringResource,
) {
    Relevant(SearchOrder.Relevant, Res.string.order_relevant),
    Latest(SearchOrder.Latest, Res.string.order_latest),
}

fun SearchOrderUi.toDomain(): SearchOrder = domain

fun SearchOrder.toPresentation(): SearchOrderUi = SearchOrderUi.entries.first { it.domain == this }