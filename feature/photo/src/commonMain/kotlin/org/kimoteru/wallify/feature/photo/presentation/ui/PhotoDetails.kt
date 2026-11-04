package org.kimoteru.wallify.feature.photo.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.extensions.Bullet
import org.kimoteru.wallify.core.common.extensions.Space
import org.kimoteru.wallify.core.common.extensions.toCompactString
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.avatar.UserAvatar
import org.kimoteru.wallify.core.designsystem.ui.chart.Sparkline
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButtonStyle
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassCard
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassStat
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcon
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.info.InfoLine
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoExif
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.photo_attribution
import org.kimoteru.wallify.core.resources.generated.resources.photo_collections_empty
import org.kimoteru.wallify.core.resources.generated.resources.photo_collections_title
import org.kimoteru.wallify.core.resources.generated.resources.photo_dimensions
import org.kimoteru.wallify.core.resources.generated.resources.photo_download
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_aperture
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_aperture_value
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_camera
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_empty
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_exposure
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_exposure_value
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_focal
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_focal_value
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_iso
import org.kimoteru.wallify.core.resources.generated.resources.photo_exif_title
import org.kimoteru.wallify.core.resources.generated.resources.photo_open_source
import org.kimoteru.wallify.core.resources.generated.resources.photo_stats_downloads
import org.kimoteru.wallify.core.resources.generated.resources.photo_stats_title
import org.kimoteru.wallify.core.resources.generated.resources.photo_stats_views
import org.kimoteru.wallify.core.resources.generated.resources.photo_tags

/** Everything about the photo except the image: author, actions, statistics, camera data, tags, collections. */
@Composable
fun PhotoDetails(
    modifier: Modifier = Modifier,
    photo: Photo,
    uiState: PhotoContract.UiState,
    obtainEvent: (PhotoContract.UiEvent) -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(appPadding.largeSpecial)
    ) {
        photo.user?.let { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .debouncedClickable(onClick = { obtainEvent(PhotoContract.UiEvent.OpenAuthor) }),
                horizontalArrangement = Arrangement.spacedBy(appPadding.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    imageUrl = user.profileImage?.medium,
                    name = user.name
                )

                Text(
                    text = stringResource(Res.string.photo_attribution, user.name),
                    style = appTypography.h6.semiBold,
                    color = appColor.text.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        photo.description?.let { description ->
            Text(
                text = description,
                style = appTypography.h5.regular,
                color = appColor.text.common
            )
        }

        Text(
            text = listOfNotNull(
                stringResource(Res.string.photo_dimensions, photo.width, photo.height),
                photo.location
            ).joinToString(" ${String.Bullet} "),
            style = appTypography.h7.medium,
            color = appColor.text.subtitle
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial),
            verticalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial)
        ) {
            GlassButton(
                text = stringResource(Res.string.photo_download),
                style = GlassButtonStyle.Solid,
                icon = AppIcons.Download,
                enabled = !uiState.isDownloading,
                onClick = { obtainEvent(PhotoContract.UiEvent.Download) }
            )

            if (photo.htmlUrl != null) {
                GlassButton(
                    text = stringResource(Res.string.photo_open_source),
                    icon = AppIcons.OpenInNew,
                    onClick = { obtainEvent(PhotoContract.UiEvent.OpenSource) }
                )
            }
        }

        PhotoStatistics(uiState = uiState)

        PhotoExifCard(exif = photo.exif)

        if (photo.tags.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(appPadding.medium)) {
                SectionTitle(text = stringResource(Res.string.photo_tags))

                Text(
                    text = photo.tags.joinToString(" ${String.Bullet} "),
                    style = appTypography.h6.regular,
                    color = appColor.text.common
                )
            }
        }

        PhotoCollections(
            uiState = uiState,
            obtainEvent = obtainEvent
        )
    }
}

@Composable
private fun PhotoStatistics(uiState: PhotoContract.UiState) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    val statistics = uiState.statistics ?: return

    Column(verticalArrangement = Arrangement.spacedBy(appPadding.medium)) {
        SectionTitle(text = stringResource(Res.string.photo_stats_title))

        Row(horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial)) {
            GlassStat(
                modifier = Modifier.weight(1f),
                value = statistics.views.change.toCompactString(),
                label = stringResource(Res.string.photo_stats_views)
            )

            GlassStat(
                modifier = Modifier.weight(1f),
                value = statistics.downloads.change.toCompactString(),
                label = stringResource(Res.string.photo_stats_downloads)
            )
        }

        Sparkline(
            modifier = Modifier
                .fillMaxWidth()
                .height(appSize.chart.sparklineHeight),
            values = statistics.views.values.map { point -> point.value }
        )
    }
}

@Composable
private fun PhotoExifCard(exif: PhotoExif?) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val camera = listOfNotNull(exif?.make, exif?.model).joinToString(String.Space)

    val rows = listOfNotNull(
        camera.takeIf { it.isNotBlank() }?.let { stringResource(Res.string.photo_exif_camera) to it },
        exif?.aperture?.let {
            stringResource(Res.string.photo_exif_aperture) to stringResource(Res.string.photo_exif_aperture_value, it)
        },
        exif?.exposureTime?.let {
            stringResource(Res.string.photo_exif_exposure) to stringResource(Res.string.photo_exif_exposure_value, it)
        },
        exif?.iso?.let { stringResource(Res.string.photo_exif_iso) to it.toString() },
        exif?.focalLength?.let {
            stringResource(Res.string.photo_exif_focal) to stringResource(Res.string.photo_exif_focal_value, it)
        }
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        level = GlassLevel.Sub,
        paddingValues = PaddingValues(appPadding.largeSpecial)
    ) {
        SectionTitle(text = stringResource(Res.string.photo_exif_title))

        if (rows.isEmpty()) {
            Text(
                modifier = Modifier.padding(top = appPadding.medium),
                text = stringResource(Res.string.photo_exif_empty),
                style = appTypography.h6.regular,
                color = appColor.text.subtitle
            )
        } else {
            rows.forEach { (title, value) ->
                InfoLine(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = appPadding.medium),
                    title = title,
                    subtitle = value
                )
            }
        }
    }
}

@Composable
private fun PhotoCollections(
    uiState: PhotoContract.UiState,
    obtainEvent: (PhotoContract.UiEvent) -> Unit
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(appPadding.medium)) {
        SectionTitle(text = stringResource(Res.string.photo_collections_title))

        if (uiState.collections.isEmpty()) {
            Text(
                text = stringResource(Res.string.photo_collections_empty),
                style = appTypography.h6.regular,
                color = appColor.text.subtitle
            )
        }

        uiState.collections.forEach { collection ->
            val isMember = collection.id in uiState.memberCollectionIds
            val isPending = collection.id in uiState.pendingCollectionIds

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                level = GlassLevel.Sub,
                corners = appCorner.medium,
                paddingValues = PaddingValues(appPadding.medium),
                onClick = {
                    if (!isPending) obtainEvent(PhotoContract.UiEvent.ToggleCollection(collection.id))
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(appPadding.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = collection.title,
                        style = appTypography.h6.medium,
                        color = appColor.text.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AppIcon(
                        icon = if (isMember) AppIcons.Check else AppIcons.Add,
                        tint = if (isMember) appColor.text.title else appColor.text.subtitle,
                        size = appSize.icon.mediumSpecial
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val appColor = AppTheme.color
    val appTypography = AppTheme.typography

    Text(
        text = text.uppercase(),
        style = appTypography.caps.semiBold,
        color = appColor.text.subtitle
    )
}