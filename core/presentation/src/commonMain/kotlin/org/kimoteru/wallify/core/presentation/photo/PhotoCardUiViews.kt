package org.kimoteru.wallify.core.presentation.photo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.avatar.UserAvatar
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoCard
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode

/** [PhotoCard] of a card the logic has built: the screen passes the model and the click, nothing else. */
@Composable
fun PhotoCard(
    modifier: Modifier = Modifier,
    card: PhotoCardUi,
    mode: PhotoGridMode = LocalPhotoGridMode.current,
    onClick: () -> Unit
) {
    PhotoCard(
        modifier = modifier,
        imageUrl = card.imageUrl,
        lowImageUrl = card.lowImageUrl,
        aspectRatio = card.aspectRatio,
        colorArgb = card.colorArgb,
        authorName = card.author?.name,
        authorInitials = card.author?.initials,
        authorAvatarUrl = card.author?.imageUrl,
        mode = mode,
        onClick = onClick
    )
}

/** [UserAvatar] of an avatar the logic has built. */
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    avatar: AvatarUi,
    size: Dp = AppTheme.size.avatar.medium
) {
    UserAvatar(
        modifier = modifier,
        imageUrl = avatar.imageUrl,
        initials = avatar.initials,
        size = size
    )
}