package org.kimoteru.wallify.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.extensions.Bullet
import org.kimoteru.wallify.core.common.extensions.toCompactString
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.avatar.UserAvatar
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButtonStyle
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassSegmentedControl
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassStat
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorTitle
import org.kimoteru.wallify.core.designsystem.ui.shimmer.ShimmerBox
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_retry
import org.kimoteru.wallify.core.resources.generated.resources.profile_edit
import org.kimoteru.wallify.core.resources.generated.resources.profile_stat_collections
import org.kimoteru.wallify.core.resources.generated.resources.profile_stat_downloads
import org.kimoteru.wallify.core.resources.generated.resources.profile_stat_photos
import org.kimoteru.wallify.core.resources.generated.resources.profile_tab_collections
import org.kimoteru.wallify.core.resources.generated.resources.profile_tab_photos
import org.kimoteru.wallify.core.resources.generated.resources.profile_upload

/** Avatar, name, short bio, counters and the tab switch of a profile. */
@Composable
fun ProfileHeader(
    modifier: Modifier = Modifier,
    uiState: ProfileContract.UiState,
    obtainEvent: (ProfileContract.UiEvent) -> Unit
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val user = uiState.user

    // On a wide window the header stays as wide as a text screen instead of stretching over the whole grid.
    Column(
        modifier = modifier
            .widthIn(max = appSize.panel.contentMaxWidth)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appPadding.medium)
    ) {
        if (user == null) {
            if (uiState.hasError && !uiState.isLoading) {
                Text(
                    text = networkErrorTitle(),
                    style = appTypography.h4.semiBold,
                    color = appColor.text.title
                )

                GlassButton(
                    text = stringResource(Res.string.common_retry),
                    onClick = { obtainEvent(ProfileContract.UiEvent.Retry) }
                )
            } else {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(appSize.avatar.profile),
                    shape = RoundedCornerShape(appCorner.large + appCorner.smallSpecial)
                )
            }
        } else {
            UserAvatar(
                imageUrl = user.profileImage?.large,
                name = user.name,
                size = appSize.avatar.profile
            )

            Text(
                text = user.name,
                style = appTypography.h3.bold,
                color = appColor.text.title,
                textAlign = TextAlign.Center
            )

            Text(
                text = listOfNotNull("@${user.username}", user.location).joinToString(" ${String.Bullet} "),
                style = appTypography.h7.medium,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )

            user.bio?.let { bio ->
                Text(
                    text = bio,
                    style = appTypography.h6.regular,
                    color = appColor.text.common,
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.isOwn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(appPadding.small)
                ) {
                    GlassButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.profile_edit),
                        icon = AppIcons.Edit,
                        onClick = { obtainEvent(ProfileContract.UiEvent.ShowEdit) }
                    )

                    GlassButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.profile_upload),
                        style = GlassButtonStyle.Solid,
                        icon = AppIcons.Upload,
                        onClick = { obtainEvent(ProfileContract.UiEvent.Upload) }
                    )
                }
            }

            // The tiles are one height whatever their labels take.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(appPadding.small)
            ) {
                GlassStat(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    value = user.totalPhotos.toLong().toCompactString(),
                    label = stringResource(Res.string.profile_stat_photos)
                )

                GlassStat(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    value = user.totalCollections.toLong().toCompactString(),
                    label = stringResource(Res.string.profile_stat_collections)
                )

                uiState.statistics?.let { statistics ->
                    GlassStat(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        value = statistics.downloads.total.toCompactString(),
                        label = stringResource(Res.string.profile_stat_downloads)
                    )
                }
            }
        }

        GlassSegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = listOf(
                stringResource(Res.string.profile_tab_photos),
                stringResource(Res.string.profile_tab_collections)
            ),
            selectedIndex = uiState.selectedTab.ordinal,
            onSelect = { index ->
                obtainEvent(ProfileContract.UiEvent.SelectTab(ProfileContract.Tab.entries[index]))
            }
        )
    }
}