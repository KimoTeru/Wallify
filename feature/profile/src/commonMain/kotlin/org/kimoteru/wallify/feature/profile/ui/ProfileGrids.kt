package org.kimoteru.wallify.feature.profile.ui

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
import org.kimoteru.wallify.core.designsystem.ui.paging.isAppending
import org.kimoteru.wallify.core.designsystem.ui.paging.isEmpty
import org.kimoteru.wallify.core.designsystem.ui.paging.isRefreshing
import org.kimoteru.wallify.core.designsystem.ui.paging.keyAt
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridLayout
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_photos_empty
import org.kimoteru.wallify.core.resources.generated.resources.profile_collection_back
import org.kimoteru.wallify.core.resources.generated.resources.profile_collection_photos
import org.kimoteru.wallify.core.resources.generated.resources.profile_collections_empty

private const val HEADER_KEY = "profile_header"
private const val SUB_HEADER_KEY = "profile_sub_header"

/**
 * Photos of the user, or of one of the user's collections when [collection] is set. The profile header scrolls
 * with the photos; for a collection its name and a way back to the list sit under the header.
 */
@Composable
fun ProfilePhotosGrid(
    photos: Flow<PagingData<Photo>>,
    contentPadding: PaddingValues,
    header: @Composable () -> Unit,
    collection: PhotoCollection? = null,
    obtainEvent: (ProfileContract.UiEvent) -> Unit
) {
    val paging = photos.collectAsLazyPagingItems()
    val emptyText = stringResource(Res.string.common_photos_empty)

    PhotoGridLayout(contentPadding = contentPadding) {
        item(key = HEADER_KEY, span = StaggeredGridItemSpan.FullLine) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                header()
            }
        }

        collection?.let {
            item(key = SUB_HEADER_KEY, span = StaggeredGridItemSpan.FullLine) {
                CollectionTitle(
                    collection = it,
                    onBack = { obtainEvent(ProfileContract.UiEvent.CloseCollection) }
                )
            }
        }

        items(
            count = paging.itemCount,
            key = { index -> paging.keyAt(index) { photo -> photo.id } }
        ) { index ->
            paging[index]?.let { photo ->
                PhotoCard(
                    imageUrl = photo.urls.regular,
                    width = photo.width,
                    height = photo.height,
                    colorHex = photo.color,
                    authorName = null,
                    authorAvatarUrl = null,
                    onClick = { obtainEvent(ProfileContract.UiEvent.OpenPhoto(photo.id)) }
                )
            }
        }

        statusItems(paging = paging, emptyText = emptyText)
    }
}

@Composable
fun ProfileCollectionsGrid(
    collections: Flow<PagingData<PhotoCollection>>,
    contentPadding: PaddingValues,
    header: @Composable () -> Unit,
    obtainEvent: (ProfileContract.UiEvent) -> Unit
) {
    val paging = collections.collectAsLazyPagingItems()
    val emptyText = stringResource(Res.string.profile_collections_empty)

    PhotoGridLayout(contentPadding = contentPadding) {
        item(key = HEADER_KEY, span = StaggeredGridItemSpan.FullLine) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                header()
            }
        }

        items(
            count = paging.itemCount,
            key = { index -> paging.keyAt(index) { collection -> collection.id } }
        ) { index ->
            paging[index]?.let { collection ->
                CollectionCard(
                    collection = collection,
                    onClick = { obtainEvent(ProfileContract.UiEvent.OpenCollection(collection)) }
                )
            }
        }

        statusItems(paging = paging, emptyText = emptyText)
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
    collection: PhotoCollection,
    onClick: () -> Unit
) {
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
            model = collection.coverPhoto?.urls?.small
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