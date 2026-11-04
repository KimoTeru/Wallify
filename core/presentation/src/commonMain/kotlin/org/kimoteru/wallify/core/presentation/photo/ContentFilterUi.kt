package org.kimoteru.wallify.core.presentation.photo

import org.jetbrains.compose.resources.StringResource
import org.kimoteru.wallify.core.model.photo.ContentFilter
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.content_filter_high
import org.kimoteru.wallify.core.resources.generated.resources.content_filter_low

/** Label of a content filter. */
enum class ContentFilterUi(
    val label: StringResource
) {
    Low(Res.string.content_filter_low),
    High(Res.string.content_filter_high)
}

fun ContentFilterUi.toDomain(): ContentFilter = when (this) {
    ContentFilterUi.Low -> ContentFilter.Low
    ContentFilterUi.High -> ContentFilter.High
}

fun ContentFilter.toPresentation(): ContentFilterUi = when (this) {
    ContentFilter.Low -> ContentFilterUi.Low
    ContentFilter.High -> ContentFilterUi.High
}