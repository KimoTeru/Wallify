package org.kimoteru.wallify.core.network.model.response.topic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.kimoteru.wallify.core.network.model.response.user.UserResponse

/**
 * Topic. Owners, contributors, cover and preview photos come with the list and detail endpoints.
 * `currentUserContributions` and `totalCurrentUserSubmissions` have no documented shape, so they stay raw JSON.
 */
@Serializable
data class TopicResponse(
    @SerialName("id")
    val id: String,

    @SerialName("slug")
    val slug: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("published_at")
    val publishedAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("starts_at")
    val startsAt: String? = null,

    @SerialName("ends_at")
    val endsAt: String? = null,

    @SerialName("only_submissions_after")
    val onlySubmissionsAfter: String? = null,

    @SerialName("visibility")
    val visibility: String? = null,

    @SerialName("featured")
    val featured: Boolean? = null,

    @SerialName("total_photos")
    val totalPhotos: Int? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("links")
    val links: TopicLinksResponse? = null,

    @SerialName("owners")
    val owners: List<UserResponse> = emptyList(),

    @SerialName("top_contributors")
    val topContributors: List<UserResponse> = emptyList(),

    @SerialName("current_user_contributions")
    val currentUserContributions: List<JsonElement> = emptyList(),

    @SerialName("total_current_user_submissions")
    val totalCurrentUserSubmissions: JsonObject? = null,

    @SerialName("cover_photo")
    val coverPhoto: PhotoResponse? = null,

    @SerialName("preview_photos")
    val previewPhotos: List<PhotoResponse> = emptyList()
)

@Serializable
data class TopicLinksResponse(
    @SerialName("self")
    val self: String? = null,

    @SerialName("html")
    val html: String? = null,

    @SerialName("photos")
    val photos: String? = null
)

fun TopicResponse.toDomain(): Topic = Topic(
    id = id,
    slug = slug,
    title = title,
    description = description,
    totalPhotos = totalPhotos ?: 0,
    coverPhoto = coverPhoto?.toDomain()
)