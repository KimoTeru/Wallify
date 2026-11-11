package org.kimoteru.wallify.feature.profile.presentation.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassChip
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.image.CachedAsyncImage
import org.kimoteru.wallify.core.designsystem.ui.indicator.DefaultProgressIndicator
import org.kimoteru.wallify.core.designsystem.ui.paging.RefreshErrorEffect
import org.kimoteru.wallify.core.designsystem.ui.paging.isAppending
import org.kimoteru.wallify.core.designsystem.ui.paging.isEmpty
import org.kimoteru.wallify.core.designsystem.ui.paging.isRefreshing
import org.kimoteru.wallify.core.designsystem.ui.paging.keyAt
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCardPlaceholder
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridLayout
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.rememberPhotoGridState
import org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh.DefaultPullToRefreshBox
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.presentation.photo.PhotoCard
import org.kimoteru.wallify.core.presentation.photo.PhotoCardUi
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_photos_empty
import org.kimoteru.wallify.core.resources.generated.resources.profile_collection_back
import org.kimoteru.wallify.core.resources.generated.resources.profile_collection_photos
import org.kimoteru.wallify.core.resources.generated.resources.profile_collections_empty
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileContract

private const val HEADER_KEY = "profile_header"
private const val SUB_HEADER_KEY = "profile_sub_header"
private const val PHOTO_CONTENT_TYPE = "photo"
private const val COLLECTION_CONTENT_TYPE = "collection"
private const val PLACEHOLDER_CONTENT_TYPE = "placeholder"

/**
 * Photos and collections of one profile in a single grid of equal cards ([PhotoGridMode.Grid]) whatever layout the
 * feed uses. The header stays the same item when the tab changes, so the segmented control slides instead of being
 * created again on the selected tab.
 */
@Composable
internal fun ProfileGrid(
    modifier: Modifier = Modifier,
    uiState: ProfileContract.UiState,
    photos: Flow<PagingData<PhotoCardUi>>,
    collections: Flow<PagingData<ProfileContract.CollectionCardUi>>,
    collectionPhotos: Flow<PagingData<PhotoCardUi>>,
    contentPadding: PaddingValues,
    obtainEvent: (ProfileContract.UiEvent) -> Unit
) {
    val appRatio = AppTheme.ratio

    val openedCollection = uiState.shownCollection
    val photoPaging = when {
        uiState.selectedTab == ProfileContract.Tab.Photos -> photos.collectAsLazyPagingItems()
        openedCollection != null -> collectionPhotos.collectAsLazyPagingItems()
        else -> null
    }
    val collectionPaging = if (photoPaging == null) collections.collectAsLazyPagingItems() else null
    val emptyText = stringResource(
        if (collectionPaging != null) Res.string.profile_collections_empty else Res.string.common_photos_empty
    )

    // The photos, the collections and the photos of a collection are three lists in one grid: each opens at the top.
    val gridState = rememberPhotoGridState(uiState.selectedTab, openedCollection?.id)

    // Pull to refresh asks the backend for the list on screen, whatever its age.
    val listPaging = photoPaging ?: collectionPaging

    listPaging?.let { paging ->
        RefreshErrorEffect(
            paging = paging,
            retries = uiState.listRetries,
            onChange = { list, error -> obtainEvent(ProfileContract.UiEvent.ListRefreshError(list, error)) }
        )
    }

    DefaultPullToRefreshBox(
        modifier = modifier,
        isRefreshing = listPaging?.let { it.isRefreshing && it.itemCount > 0 } == true,
        onRefresh = { listPaging?.refresh() },
        indicatorPadding = PaddingValues(top = contentPadding.calculateTopPadding())
    ) {
        PhotoGridLayout(
            modifier = Modifier.fillMaxSize(),
            mode = PhotoGridMode.Grid,
            state = gridState,
            contentPadding = contentPadding
        ) {
            item(key = HEADER_KEY, span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ProfileHeader(
                        uiState = uiState,
                        obtainEvent = obtainEvent
                    )
                }
            }

            openedCollection?.let { collection ->
                item(key = SUB_HEADER_KEY, span = StaggeredGridItemSpan.FullLine) {
                    CollectionTitle(
                        collection = collection,
                        onBack = { obtainEvent(ProfileContract.UiEvent.CloseCollection) }
                    )
                }
            }

            photoPaging?.let { paging ->
                items(
                    count = paging.itemCount,
                    key = { index -> paging.keyAt(index) { card -> card.id } },
                    // A type per kind of item lets the grid reuse the nodes of a card for another card.
                    contentType = { index ->
                        if (paging.peek(index) != null) PHOTO_CONTENT_TYPE else PLACEHOLDER_CONTENT_TYPE
                    }
                ) { index ->
                    val card = paging[index]

                    if (card != null) {
                        PhotoCard(
                            card = card,
                            mode = PhotoGridMode.Grid,
                            onClick = { obtainEvent(ProfileContract.UiEvent.OpenPhoto(card.id)) }
                        )
                    } else {
                        PhotoCardPlaceholder(mode = PhotoGridMode.Grid)
                    }
                }

                statusItems(paging = paging, emptyText = emptyText)
            }

            collectionPaging?.let { paging ->
                items(
                    count = paging.itemCount,
                    key = { index -> paging.keyAt(index) { card -> card.collection.id } },
                    contentType = { index ->
                        if (paging.peek(index) != null) COLLECTION_CONTENT_TYPE else PLACEHOLDER_CONTENT_TYPE
                    }
                ) { index ->
                    val card = paging[index]

                    if (card != null) {
                        CollectionCard(
                            card = card,
                            onClick = { obtainEvent(ProfileContract.UiEvent.OpenCollection(card.collection)) }
                        )
                    } else {
                        PhotoCardPlaceholder(
                            mode = PhotoGridMode.Grid,
                            aspectRatio = appRatio.image.landscape
                        )
                    }
                }

                statusItems(paging = paging, emptyText = emptyText)
            }
        }
    }
}

/** Loader while pages load and a message when the list is empty, as full-width items at the end of a grid. */
private fun LazyStaggeredGridScope.statusItems(
    paging: LazyPagingItems<*>,
    emptyText: String
) {
    if ((paging.isRefreshing && paging.itemCount == 0) || paging.isAppending) {
        item(span = StaggeredGridItemSpan.FullLine) {
            val appPadding = AppTheme.padding

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

    if (paging.isEmpty) {
        item(span = StaggeredGridItemSpan.FullLine) {
            val appColor = AppTheme.color
            val appPadding = AppTheme.padding
            val appTypography = AppTheme.typography

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(appPadding.large),
                text = emptyText,
                style = appTypography.h6.regular,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CollectionTitle(
    collection: PhotoCollection,
    onBack: () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassChip(
            text = stringResource(Res.string.profile_collection_back),
            icon = AppIcons.Back,
            onClick = onBack
        )

        Text(
            text = collection.title,
            style = appTypography.h5.semiBold,
            color = appColor.text.title,
            maxLines = 1
        )
    }
}

@Composable
private fun CollectionCard(
    card: ProfileContract.CollectionCardUi,
    onClick: () -> Unit
) {
    val collection = card.collection

    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appGradient = AppTheme.gradient
    val appPadding = AppTheme.padding
    val appRatio = AppTheme.ratio
    val appTypography = AppTheme.typography

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(appRatio.image.landscape)
            .clip(RoundedCornerShape(appCorner.large + appCorner.smallSpecial))
            .background(appColor.glass.sub)
            .debouncedClickable(onClick = onClick)
    ) {
        CachedAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = card.coverUrl
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(appGradient.blackOverlayGradient))
                .padding(appPadding.mediumSpecial)
        ) {
            Text(
                text = collection.title,
                style = appTypography.h6.semiBold,
                color = appColor.basic.white,
                maxLines = 1
            )

            Text(
                text = stringResource(Res.string.profile_collection_photos, collection.totalPhotos),
                style = appTypography.h8.medium,
                color = appColor.basic.white
            )
        }
    }
}