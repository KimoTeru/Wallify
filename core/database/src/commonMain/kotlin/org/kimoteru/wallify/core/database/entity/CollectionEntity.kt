package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.user.User

@Entity(tableName = "collection")
data class CollectionEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String?,
    val totalPhotos: Int,
    val isPrivate: Boolean,
    val coverPhoto: Photo?,
    val user: User?
)

fun PhotoCollection.toEntity(): CollectionEntity = CollectionEntity(
    id = id,
    title = title,
    description = description,
    totalPhotos = totalPhotos,
    isPrivate = isPrivate,
    coverPhoto = coverPhoto,
    user = user
)

fun CollectionEntity.toDomain(): PhotoCollection = PhotoCollection(
    id = id,
    title = title,
    description = description,
    totalPhotos = totalPhotos,
    isPrivate = isPrivate,
    coverPhoto = coverPhoto,
    user = user
)