package org.kimoteru.wallify.core.model.user

import kotlinx.serialization.Serializable

/**
 * User. Inside photos and collections the backend sends a short form, where the counters are 0.
 * [email] comes only for the current user. [profileUrl] is the profile page that attribution links to.
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val name: String,
    val firstName: String?,
    val lastName: String?,
    val bio: String?,
    val location: String?,
    val portfolioUrl: String?,
    val instagramUsername: String?,
    val email: String?,
    val totalPhotos: Int,
    val totalCollections: Int,
    val profileImage: ProfileImage?,
    val profileUrl: String?
)

@Serializable
data class ProfileImage(
    val small: String,
    val medium: String,
    val large: String
)

/** Changes of the current user's profile; `null` fields stay as they are. */
data class UpdateUserParams(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val portfolioUrl: String? = null,
    val location: String? = null,
    val bio: String? = null,
    val instagramUsername: String? = null
)