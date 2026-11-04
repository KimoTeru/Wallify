package org.kimoteru.wallify.core.designsystem.ui.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.common.extensions.Space
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.designsystem.ui.glass.glassSurface
import org.kimoteru.wallify.core.designsystem.ui.image.CachedAsyncImage

/** Round profile picture; shows the initials of [name] while there is no [imageUrl] or the image is loading. */
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    name: String,
    size: Dp = AppTheme.size.avatar.medium
) {
    val appColor = AppTheme.color
    val appTypography = AppTheme.typography

    val initials = name
        .split(String.Space)
        .mapNotNull { word -> word.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString(String.Empty)

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