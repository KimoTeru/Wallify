package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.kimoteru.wallify.core.model.user.ProfileImage
import org.kimoteru.wallify.core.model.user.User

/**
 * Saved user. [isCurrent] marks the signed-in user, so `UserRepository.currentUser` comes from the database.
 * [isDetailed] is `true` for a profile loaded by its own request; a summary (search, short form) never overwrites it.
 */
@Entity(
    tableName = "user",
    indices = [Index("username"), Index("isCurrent")]
)
data class UserEntity(
    @PrimaryKey val id: String,
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
    val profileUrl: String?,
    val isCurrent: Boolean,
    val isDetailed: Boolean
)

fun User.toEntity(isCurrent: Boolean, isDetailed: Boolean): UserEntity = UserEntity(
    id = id,
    username = username,
    name = name,
    firstName = firstName,
    lastName = lastName,
    bio = bio,
    location = location,
    portfolioUrl = portfolioUrl,
    instagramUsername = instagramUsername,
    email = email,
    totalPhotos = totalPhotos,
    totalCollections = totalCollections,
    profileImage = profileImage,
    profileUrl = profileUrl,
    isCurrent = isCurrent,
    isDetailed = isDetailed
)

fun UserEntity.toDomain(): User = User(
    id = id,
    username = username,
    name = name,
    firstName = firstName,
    lastName = lastName,
    bio = bio,
    location = location,
    portfolioUrl = portfolioUrl,
    instagramUsername = instagramUsername,
    email = email,
    totalPhotos = totalPhotos,
    totalCollections = totalCollections,
    profileImage = profileImage,
    profileUrl = profileUrl
)