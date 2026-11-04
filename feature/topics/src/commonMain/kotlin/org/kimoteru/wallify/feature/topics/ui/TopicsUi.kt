package org.kimoteru.wallify.feature.topics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassChip
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorDescription
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorTitle
import org.kimoteru.wallify.core.designsystem.ui.paging.isRefreshing
import org.kimoteru.wallify.core.designsystem.ui.paging.keyAt
import org.kimoteru.wallify.core.designsystem.ui.paging.refreshError
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PagingPhotoGrid
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMessage
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridShimmer
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_photos_empty
import org.kimoteru.wallify.core.resources.generated.resources.common_retry
import org.kimoteru.wallify.core.resources.generated.resources.nav_topics

object TopicsContract {
    @Immutable
    data class UiState(
        val selectedSlug: String? = null
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data class SelectTopic(val slug: String) : UiEvent
        data class OpenPhoto(val photoId: String) : UiEvent
    }
}

@Preview
@Composable
fun PreviewTopicsUi() {
    AppTheme {
        TopicsUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : TopicsComponent {
                override val topics = flowOf(PagingData.empty<Topic>())
                override val photos = flowOf(PagingData.empty<Photo>())

                @Composable
                override fun getUiState(): TopicsContract.UiState = TopicsContract.UiState()
            }
        )
    }
}

@Composable
fun TopicsUi(
    modifier: Modifier = Modifier,
    component: TopicsComponent
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    val topics = component.topics.collectAsLazyPagingItems()
    val photos = component.photos.collectAsLazyPagingItems()
    val screenPadding = rememberScreenPadding()

    val firstTopic = topics.itemSnapshotList.firstOrNull()

    LaunchedEffect(firstTopic?.slug) {
        if (uiState.selectedSlug == null && firstTopic != null) {
            obtainEvent(TopicsContract.UiEvent.SelectTopic(firstTopic.slug))
        }
    }

    FloatingBarScreen(
        modifier = modifier,
        bar = {
            Column {
                Text(
                    modifier = Modifier.padding(screenPadding.header),
                    text = stringResource(Res.string.nav_topics),
                    style = appTypography.h2.bold,
                    color = appColor.text.title
                )

                LazyRow(
                    modifier = Modifier.padding(top = appPadding.medium),
                    contentPadding = PaddingValues(horizontal = screenPadding.start),
                    horizontalArrangement = Arrangement.spacedBy(appPadding.small)
                ) {
                    items(
                        count = topics.itemCount,
                        key = { index -> topics.keyAt(index) { it.id } }
                    ) { index ->
                        topics[index]?.let { topic ->
                            GlassChip(
                                text = topic.title,
                                selected = topic.slug == uiState.selectedSlug,
                                level = GlassLevel.Floating,
                                onClick = { obtainEvent(TopicsContract.UiEvent.SelectTopic(topic.slug)) }
                            )
                        }
                    }
                }
            }
        }
    ) { topPadding ->
        val contentPadding = PaddingValues(
            start = screenPadding.start,
            top = topPadding,
            end = screenPadding.end,
            bottom = screenPadding.bottom
        )

        // The topics came back after an offline start: select the first one again without a tap.
        RetryOnReconnect(
            isFailed = topics.itemCount == 0 && topics.refreshError != null,
            onRetry = { topics.retry() }
        )

        // The photos of a topic are a grid of equal cards whatever layout the feed uses.
        CompositionLocalProvider(LocalPhotoGridMode provides PhotoGridMode.Grid) {
            when {
                topics.itemCount == 0 && topics.refreshError != null -> PhotoGridMessage(
                    title = networkErrorTitle(),
                    description = networkErrorDescription(),
                    actionText = stringResource(Res.string.common_retry),
                    onAction = { topics.retry() }
                )

                topics.itemCount == 0 && topics.isRefreshing -> PhotoGridShimmer(contentPadding = contentPadding)

                else -> PagingPhotoGrid(
                    modifier = Modifier.fillMaxSize(),
                    paging = photos,
                    itemKey = { photo -> photo.id },
                    emptyText = stringResource(Res.string.common_photos_empty),
                    contentPadding = contentPadding
                ) { photo ->
                    PhotoCard(
                        imageUrl = photo.urls.regular,
                        width = photo.width,
                        height = photo.height,
                        colorHex = photo.color,
                        authorName = photo.user?.name,
                        authorAvatarUrl = photo.user?.profileImage?.small,
                        onClick = { obtainEvent(TopicsContract.UiEvent.OpenPhoto(photo.id)) }
                    )
                }
            }
        }
    }
}