package org.kimoteru.wallify.feature.topics.presentation.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassChip
import org.kimoteru.wallify.core.designsystem.ui.paging.keyAt
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.feature.topics.presentation.ui.TopicsContract

/** Horizontal list of topic chips. The selected slug is highlighted. */
@Composable
internal fun TopicChips(
    modifier: Modifier = Modifier,
    topics: LazyPagingItems<Topic>,
    selectedSlug: String?,
    contentPadding: PaddingValues = PaddingValues(),
    obtainEvent: (TopicsContract.UiEvent) -> Unit
) {
    val appPadding = AppTheme.padding

    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(appPadding.small)
    ) {
        items(
            count = topics.itemCount,
            key = { index -> topics.keyAt(index) { it.id } }
        ) { index ->
            topics[index]?.let { topic ->
                GlassChip(
                    text = topic.title,
                    selected = topic.slug == selectedSlug,
                    onClick = { obtainEvent(TopicsContract.UiEvent.SelectTopic(topic.slug)) }
                )
            }
        }
    }
}