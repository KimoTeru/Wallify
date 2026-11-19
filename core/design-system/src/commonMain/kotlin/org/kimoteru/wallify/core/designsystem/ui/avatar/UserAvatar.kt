package org.kimoteru.wallify.core.designsystem.ui.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.designsystem.ui.glass.glassSurface
import org.kimoteru.wallify.core.designsystem.ui.image.CachedAsyncImage

/**
 * Round profile picture; shows [initials] while there is no [imageUrl] or the image is loading. The initials come
 * ready from the logic of the screen (`AvatarUi` of `core:presentation`): the avatar computes nothing.
 */
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    initials: String,
    size: Dp = AppTheme.size.avatar.medium
) {
    val appColor = AppTheme.color
    val appTypography = AppTheme.typography

    Box(
        modifier = modifier
            .size(size)
            .glassSurface(GlassLevel.Elevated, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = appTypography.h8.semiBold,
            color = appColor.text.title
        )

        CachedAsyncImage(
            modifier = Modifier.size(size),
            model = imageUrl
        )
    }
}