package org.kimoteru.wallify.core.presentation.photo

import androidx.compose.runtime.Immutable
import org.kimoteru.wallify.core.common.extensions.At
import org.kimoteru.wallify.core.common.extensions.Bullet
import org.kimoteru.wallify.core.common.extensions.Space
import org.kimoteru.wallify.core.common.extensions.initials
import org.kimoteru.wallify.core.common.extensions.toArgbOrNull
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.user.User

/**
 * A photo card ready to draw: the addresses are already picked for the quality of the screen, the color is parsed and
 * the proportions are counted. The logic of the screen builds it (`toCardUi`), the Ui only passes the fields to
 * `PhotoCard`, so nothing is computed while a list scrolls.
 *
 * [aspectRatio] is width / height of the photo, `null` when the size is unknown. [colorArgb] is the dominant color
 * (`0xFFRRGGBB`), `null` when the backend sent none.
 */
@Immutable
data class PhotoCardUi(
    val id: String,
    val imageUrl: String,
    val lowImageUrl: String,
    val aspectRatio: Float?,
    val colorArgb: Long?,
    val author: AvatarUi?,
)

/** A round avatar ready to draw: [initials] stand in while there is no [imageUrl] or it is loading. */
@Immutable
data class AvatarUi(
    val name: String,
    val initials: String,
    val imageUrl: String?,
)

/**
 * The card of this photo. [imageUrl] and [lowImageUrl] are picked by the logic for its quality (`forQuality` of
 * `core:domain`); without [withAuthor] the card shows no author (the photos of a profile are all the same author).
 */
fun Photo.toCardUi(
    imageUrl: String,
    lowImageUrl: String,
    withAuthor: Boolean = true,
): PhotoCardUi = PhotoCardUi(
    id = id,
    imageUrl = imageUrl,
    lowImageUrl = lowImageUrl,
    aspectRatio = aspectRatioOrNull(),
    colorArgb = color.toArgbOrNull(),
    author = user?.takeIf { withAuthor }?.let { author -> author.toAvatarUi(imageUrl = author.profileImage?.small) },
)

/** The avatar of this user with the picture [imageUrl] of the size the screen shows. */
fun User.toAvatarUi(imageUrl: String?): AvatarUi = AvatarUi(
    name = name,
    initials = name.initials(),
    imageUrl = imageUrl,
)

/** The username as the user is mentioned: `@jane`. */
fun User.handle(): String = String.At + username

/** The line under the name of a profile: the handle and, when known, the location, `@jane • Oslo`. */
fun User.profileSubtitle(): String =
    listOfNotNull(handle(), location).joinToString(String.Space + String.Bullet + String.Space)

/** Width / height of the photo, `null` when the backend sent no size. */
fun Photo.aspectRatioOrNull(): Float? = if (width > 0 && height > 0) width / height.toFloat() else null