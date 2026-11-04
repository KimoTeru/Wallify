package org.kimoteru.wallify.core.presentation.photo

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.model.photo.SearchOrder
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.order_latest
import org.kimoteru.wallify.core.resources.generated.resources.order_relevant

/** Label of a search order. */
enum class SearchOrderUi(
    val label: StringResource
) {
    Relevant(Res.string.order_relevant),
    Latest(Res.string.order_latest)
}

fun SearchOrderUi.toDomain(): SearchOrder = when (this) {
    SearchOrderUi.Relevant -> SearchOrder.Relevant
    SearchOrderUi.Latest -> SearchOrder.Latest
}

fun SearchOrder.toPresentation(): SearchOrderUi = when (this) {
    SearchOrder.Relevant -> SearchOrderUi.Relevant
    SearchOrder.Latest -> SearchOrderUi.Latest
}