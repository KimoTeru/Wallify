package org.kimoteru.wallify.core.designsystem.ui.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha
import org.kimoteru.wallify.core.designsystem.ui.avatar.UserAvatar
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.image.CachedAsyncImage

private const val MIN_ASPECT_RATIO = 0.6f
private const val MAX_ASPECT_RATIO = 1.6f
private const val HEX_RADIX = 16

/**
 * Pebble card of the photo masonry: the photo with the author in a pill on the bottom edge.
 * [colorHex] is the dominant color of the photo (`#RRGGBB`), shown until the image is loaded.
 * The card takes the proportions of the photo ([width] and [height] in pixels); extreme ones are limited,
 * so one very tall or wide photo does not break the grid.
 */
@Composable
fun PhotoCard(
    modifier: Modifier = Modifier,
    imageUrl: String,
    width: Int,
    height: Int,
    colorHex: String?,
    authorName: String?,
    authorAvatarUrl: String?,
    onClick: () -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appBorderWidth = AppTheme.borderWidth
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appRatio = AppTheme.ratio
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val placeholderColor = remember(colorHex) { colorHex?.toColorOrNull() } ?: appColor.glass.standard
    val naturalRatio = if (width > 0 && height > 0) width / height.toFloat() else appRatio.image.square
    val aspectRatio = when (LocalPhotoGridMode.current) {
        PhotoGridMode.Grid -> appRatio.image.portrait
        PhotoGridMode.Masonry, PhotoGridMode.List -> naturalRatio
    }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio.coerceIn(MIN_ASPECT_RATIO, MAX_ASPECT_RATIO))
            .clip(RoundedCornerShape(appCorner.large + appCorner.smallSpecial))
            .background(placeholderColor)
            .debouncedClickable(onClick = onClick)
    ) {
        CachedAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = imageUrl
        )

        if (authorName != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(appPadding.mediumSpecial)
                    .clip(CircleShape)
                    .background(appColor.basic.dark.withAlpha(appAlpha.largeExtra))
                    .border(appBorderWidth.small, appColor.glass.edge, CircleShape)
                    .padding(
                        start = appPadding.smallExtra,
                        top = appPadding.smallExtra,
                        end = appPadding.mediumSpecial,
                        bottom = appPadding.smallExtra
                    ),
                horizontalArrangement = Arrangement.spacedBy(appPadding.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    imageUrl = authorAvatarUrl,
                    name = authorName,
                    size = appSize.avatar.small
                )

                Text(
                    text = authorName,
                    style = appTypography.h7.medium,
                    color = appColor.basic.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** `#RRGGBB` to an opaque color, or `null` for anything else. */
private fun String.toColorOrNull(): Color? =
    removePrefix("#")
        .takeIf { it.length == 6 }
        ?.toLongOrNull(HEX_RADIX)
        ?.let { rgb -> Color(0xFF000000 or rgb) }