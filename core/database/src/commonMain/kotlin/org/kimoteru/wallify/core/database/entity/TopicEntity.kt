package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.topic.Topic

@Entity(
    tableName = "topic",
    indices = [Index("slug")]
)
data class TopicEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val description: String?,
    val totalPhotos: Int,
    val coverPhoto: Photo?
)

fun Topic.toEntity(): TopicEntity = TopicEntity(
    id = id,
    slug = slug,
    title = title,
    description = description,
    totalPhotos = totalPhotos,
    coverPhoto = coverPhoto
)

fun TopicEntity.toDomain(): Topic = Topic(
    id = id,
    slug = slug,
    title = title,
    description = description,
    totalPhotos = totalPhotos,
    coverPhoto = coverPhoto
)