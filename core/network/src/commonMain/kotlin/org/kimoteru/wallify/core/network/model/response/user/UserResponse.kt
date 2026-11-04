package org.kimoteru.wallify.core.network.model.response.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.common.extensions.Space
import org.kimoteru.wallify.core.common.extensions.nullIfBlank
import org.kimoteru.wallify.core.model.user.ProfileImage
import org.kimoteru.wallify.core.model.user.User

/**
 * User of any endpoint: the short form inside photos and collections, the public profile, or the current user
 * (`email` and `uploadsRemaining` come only there).
 */
@Serializable
data class UserResponse(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    @SerialName("twitter_username")
    val twitterUsername: String? = null,

    @SerialName("instagram_username")
    val instagramUsername: String? = null,

    @SerialName("portfolio_url")
    val portfolioUrl: String? = null,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("total_collections")
    val totalCollections: Int? = null,

    @SerialName("total_photos")
    val totalPhotos: Int? = null,

    @SerialName("downloads")
    val downloads: Int? = null,

    @SerialName("uploads_remaining")
    val uploadsRemaining: Int? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("accepted_tos")
    val acceptedTos: Boolean? = null,

    @SerialName("social")
    val social: UserSocialResponse? = null,

    @SerialName("profile_image")
    val profileImage: ProfileImageResponse? = null,

    @SerialName("badge")
    val badge: UserBadgeResponse? = null,

    @SerialName("links")
    val links: UserLinksResponse? = null
)

@Serializable
data class UserSocialResponse(
    @SerialName("instagram_username")
    val instagramUsername: String? = null,

    @SerialName("portfolio_url")
    val portfolioUrl: String? = null,

    @SerialName("twitter_username")
    val twitterUsername: String? = null
)

@Serializable
data class ProfileImageResponse(
    @SerialName("small")
    val small: String? = null,

    @SerialName("medium")
    val medium: String? = null,

    @SerialName("large")
    val large: String? = null
)

@Serializable
data class UserBadgeResponse(
    @SerialName("title")
    val title: String? = null,

    @SerialName("primary")
    val primary: Boolean? = null,

    @SerialName("slug")
    val slug: String? = null,

    @SerialName("link")
    val link: String? = null
)

@Serializable
data class UserLinksResponse(
    @SerialName("self")
    val self: String? = null,

    @SerialName("html")
    val html: String? = null,

    @SerialName("photos")
    val photos: String? = null,

    @SerialName("portfolio")
    val portfolio: String? = null,

    @SerialName("followers")
    val followers: String? = null,

    @SerialName("following")
    val following: String? = null
)

fun UserResponse.toDomain(): User = User(
    id = id,
    username = username,
    name = displayName(),
    firstName = firstName,
    lastName = lastName,
    bio = bio,
    location = location,
    portfolioUrl = portfolioUrl ?: social?.portfolioUrl,
    instagramUsername = instagramUsername ?: social?.instagramUsername,
    email = email,
    totalPhotos = totalPhotos ?: 0,
    totalCollections = totalCollections ?: 0,
    profileImage = profileImage?.toDomain(),
    profileUrl = links?.html
)

/** The backend sends only the sizes it has, so the missing ones are replaced by the one that exists. */
fun ProfileImageResponse.toDomain(): ProfileImage? {
    val anySize = small ?: medium ?: large ?: return null
    return ProfileImage(
        small = small ?: anySize,
        medium = medium ?: anySize,
        large = large ?: anySize
    )
}

/** The current user comes without `name`, so it is built from the first and last name. */
private fun UserResponse.displayName(): String =
    name?.nullIfBlank()
        ?: listOfNotNull(firstName, lastName).joinToString(String.Space).nullIfBlank()
        ?: username