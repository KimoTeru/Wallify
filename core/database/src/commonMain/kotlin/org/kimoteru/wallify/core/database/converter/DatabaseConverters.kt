package org.kimoteru.wallify.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoExif
import org.kimoteru.wallify.core.model.stats.StatSeries
import org.kimoteru.wallify.core.model.user.ProfileImage
import org.kimoteru.wallify.core.model.user.User

/**
 * Stores nested models and lists in one text column as JSON. `ignoreUnknownKeys` lets a newer app version read a
 * row written by an older one.
 */
class DatabaseConverters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { json.encodeToString<List<String>>(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { json.decodeFromString<List<String>>(it) }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? = value?.let { json.encodeToString<List<Int>>(it) }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? = value?.let { json.decodeFromString<List<Int>>(it) }

    @TypeConverter
    fun fromPhotoExif(value: PhotoExif?): String? = value?.let { json.encodeToString<PhotoExif>(it) }

    @TypeConverter
    fun toPhotoExif(value: String?): PhotoExif? = value?.let { json.decodeFromString<PhotoExif>(it) }

    @TypeConverter
    fun fromUser(value: User?): String? = value?.let { json.encodeToString<User>(it) }

    @TypeConverter
    fun toUser(value: String?): User? = value?.let { json.decodeFromString<User>(it) }

    @TypeConverter
    fun fromPhoto(value: Photo?): String? = value?.let { json.encodeToString<Photo>(it) }

    @TypeConverter
    fun toPhoto(value: String?): Photo? = value?.let { json.decodeFromString<Photo>(it) }

    @TypeConverter
    fun fromProfileImage(value: ProfileImage?): String? = value?.let { json.encodeToString<ProfileImage>(it) }

    @TypeConverter
    fun toProfileImage(value: String?): ProfileImage? = value?.let { json.decodeFromString<ProfileImage>(it) }

    @TypeConverter
    fun fromStatSeries(value: StatSeries?): String? = value?.let { json.encodeToString<StatSeries>(it) }

    @TypeConverter
    fun toStatSeries(value: String?): StatSeries? = value?.let { json.decodeFromString<StatSeries>(it) }
}