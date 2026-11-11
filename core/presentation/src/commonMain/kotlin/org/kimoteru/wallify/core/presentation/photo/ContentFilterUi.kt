package org.kimoteru.wallify.core.presentation.photo

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.model.photo.ContentFilter
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.content_filter_high
import org.kimoteru.wallify.core.resources.generated.resources.content_filter_low

/** Label of a content filter. */
enum class ContentFilterUi(
    internal val domain: ContentFilter,
    val label: StringResource,
) {
    Low(ContentFilter.Low, Res.string.content_filter_low),
    High(ContentFilter.High, Res.string.content_filter_high),
}

fun ContentFilterUi.toDomain(): ContentFilter = domain

fun ContentFilter.toPresentation(): ContentFilterUi = ContentFilterUi.entries.first { it.domain == this }