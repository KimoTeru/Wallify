package org.kimoteru.wallify.feature.topics.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.rememberIsShortWindow
import org.kimoteru.wallify.core.designsystem.ui.glass.FloatingBarScreen
import org.kimoteru.wallify.core.designsystem.ui.glass.scrolledPx
import org.kimoteru.wallify.core.designsystem.ui.glass.rememberScreenPadding
import org.kimoteru.wallify.core.designsystem.ui.top_bar.DefaultTopBar
import org.kimoteru.wallify.core.designsystem.ui.network.RetryOnReconnect
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorDescription
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorTitle
import org.kimoteru.wallify.core.designsystem.ui.paging.isRefreshing
import org.kimoteru.wallify.core.designsystem.ui.paging.refreshError
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PagingPhotoGrid
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMessage
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridShimmer
import org.kimoteru.wallify.core.designsystem.ui.photo.rememberPhotoGridState
import org.kimoteru.wallify.core.domain.photo.forQuality
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.settings.UserSettings
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.feature.topics.presentation.components.TopicsComponent
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_photos_empty
import org.kimoteru.wallify.core.resources.generated.resources.common_retry
import org.kimoteru.wallify.core.resources.generated.resources.nav_topics

object TopicsContract {
    @Immutable
    data class UiState(
        val selectedSlug: String? = null,
        val listImageQuality: ImageQuality = UserSettings.Default.listImageQuality
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
    val isShortWindow = rememberIsShortWindow()

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

    // Another topic is another list: it opens at the top, not at the place where the previous one was left.
    val gridState = rememberPhotoGridState(uiState.selectedSlug)

    FloatingBarScreen(
        modifier = modifier,
        scrolledPx = gridState.scrolledPx(),
        bar = {
            if (isShortWindow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(screenPadding.header),
                    horizontalArrangement = Arrangement.spacedBy(appPadding.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.nav_topics),
                        style = appTypography.h5.semiBold,
                        color = appColor.text.title,
                        maxLines = 1
                    )

                    TopicChips(
                        modifier = Modifier.weight(1f),
                        topics = topics,
                        selectedSlug = uiState.selectedSlug,
                        obtainEvent = obtainEvent
                    )
                }
            } else {
                Column {
                    DefaultTopBar(
                        title = stringResource(Res.string.nav_topics),
                        titleStyle = appTypography.h2.bold,
                        paddingValues = screenPadding.header
                    )

                    TopicChips(
                        modifier = Modifier.padding(top = appPadding.medium),
                        topics = topics,
                        selectedSlug = uiState.selectedSlug,
                        contentPadding = PaddingValues(horizontal = screenPadding.start),
                        obtainEvent = obtainEvent
                    )
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
                    state = gridState,
                    contentPadding = contentPadding
                ) { photo ->
                    PhotoCard(
                        imageUrl = photo.urls.forQuality(uiState.listImageQuality),
                        lowImageUrl = photo.urls.forQuality(ImageQuality.Low),
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