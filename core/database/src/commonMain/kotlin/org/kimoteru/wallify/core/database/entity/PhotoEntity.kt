package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoExif
import org.kimoteru.wallify.core.model.photo.PhotoUrls
import org.kimoteru.wallify.core.model.user.User

/**
 * Saved photo. [isDetailed] is `true` for a photo loaded by its own request, `false` for the summary that list
 * endpoints send (no [downloads], [exif], [tags]); a summary never overwrites a detailed photo.
 */
@Entity(tableName = "photo")
data class PhotoEntity(
    @PrimaryKey val id: String,
    val width: Int,
    val height: Int,
    val color: String,
    val blurHash: String?,
    val description: String?,
    val urlRaw: String,
    val urlFull: String,
    val urlRegular: String,
    val urlSmall: String,
    val urlThumb: String,
    val htmlUrl: String?,
    val downloadLocation: String?,
    val location: String?,
    val tags: List<String>,
    val downloads: Int?,
    val exif: PhotoExif?,
    val currentUserCollectionIds: List<Int>,
    val user: User?,
    val isDetailed: Boolean
)

fun Photo.toEntity(isDetailed: Boolean): PhotoEntity = PhotoEntity(
    id = id,
    width = width,
    height = height,
    color = color,
    blurHash = blurHash,
    description = description,
    urlRaw = urls.raw,
    urlFull = urls.full,
    urlRegular = urls.regular,
    urlSmall = urls.small,
    urlThumb = urls.thumb,
    htmlUrl = htmlUrl,
    downloadLocation = downloadLocation,
    location = location,
    tags = tags,
    downloads = downloads,
    exif = exif,
    currentUserCollectionIds = currentUserCollectionIds,
    user = user,
    isDetailed = isDetailed
)

fun PhotoEntity.toDomain(): Photo = Photo(
    id = id,
    width = width,
    height = height,
    color = color,
    blurHash = blurHash,
    description = description,
    urls = PhotoUrls(
        raw = urlRaw,
        full = urlFull,
        regular = urlRegular,
        small = urlSmall,
        thumb = urlThumb
    ),
    htmlUrl = htmlUrl,
    downloadLocation = downloadLocation,
    location = location,
    tags = tags,
    downloads = downloads,
    exif = exif,
    currentUserCollectionIds = currentUserCollectionIds,
    user = user
)