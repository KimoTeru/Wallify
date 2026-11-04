package org.kimoteru.wallify.core.model.topic

import kotlinx.serialization.Serializable
import org.kimoteru.wallify.core.model.photo.Photo

/** Topic. [id] or [slug] address it in `TopicRepository`. */
@Serializable
data class Topic(
    val id: String,
    val slug: String,
    val title: String,
    val description: String?,
    val totalPhotos: Int,
    val coverPhoto: Photo?
)

enum class TopicsOrder { Featured, Latest, Oldest, Position }