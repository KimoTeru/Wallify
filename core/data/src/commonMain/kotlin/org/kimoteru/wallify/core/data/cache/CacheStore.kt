package org.kimoteru.wallify.core.data.cache

import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.dao.TopicDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.kimoteru.wallify.core.database.entity.ListStateEntity
import org.kimoteru.wallify.core.database.entity.PhotoEntity
import org.kimoteru.wallify.core.database.entity.toEntity
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.topic.Topic
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.platform.time.TimeProvider
import org.koin.core.annotation.Singleton

/**
 * Every write of backend data into the database goes through here, because the database is what the screens read.
 * It also holds the merge rules that keep the cache correct: a summary (what list endpoints send) never wipes the
 * details of the same photo or user that were loaded before.
 *
 * A row is written only when it differs from the saved one: Room invalidates every list that reads a table on any
 * write to it, equal rows included, and a list that reloads loses its scroll position.
 */
@Singleton
class CacheStore(
    private val photoDao: PhotoDao,
    private val userDao: UserDao,
    private val collectionDao: CollectionDao,
    private val topicDao: TopicDao,
    private val statsDao: StatsDao,
    private val listDao: ListDao,
    private val time: TimeProvider
) {

    /** Saves photos from a list. Fields the list does not carry (exif, tags, downloads) keep their saved values. */
    suspend fun savePhotoSummaries(photos: List<Photo>) {
        if (photos.isEmpty()) return
        val saved = photoDao.getByIds(photos.map { it.id }).associateBy { it.id }
        val changed = photos.map { photo ->
            val old = saved[photo.id]
            val summary = photo.toEntity(isDetailed = false).keepingUrlsOf(old)
            if (old != null && old.isDetailed) {
                summary.copy(
                    tags = old.tags,
                    downloads = old.downloads,
                    exif = old.exif,
                    currentUserCollectionIds = old.currentUserCollectionIds,
                    isDetailed = true
                )
            } else {
                summary
            }
        }.filter { entity -> entity != saved[entity.id] }
        if (changed.isNotEmpty()) photoDao.upsertAll(changed)
    }

    /** Saves a short list of photos loaded in one request under [listKey], replacing what that list held. */
    suspend fun savePhotoList(listKey: String, photos: List<Photo>) {
        savePhotoSummaries(photos)
        listDao.putEntries(
            listKey = listKey,
            itemIds = photos.map { it.id },
            startPosition = 0,
            isRefresh = true
        )
        listDao.upsertState(ListStateEntity(listKey = listKey, updatedAtEpochMillis = time.nowEpochMillis()))
    }

    suspend fun savePhoto(photo: Photo) {
        val old = photoDao.get(photo.id)
        val entity = photo.toEntity(isDetailed = true).keepingUrlsOf(old)
        if (entity != old) photoDao.upsertAll(listOf(entity))
    }

    /**
     * Every endpoint sends its own copy of the image links (the tracking `ixid` differs), and a changed link makes
     * the image loader fetch the picture again: the photo on screen would blink and the loader's disk cache would miss.
     * So the links saved first stay.
     */
    private fun PhotoEntity.keepingUrlsOf(old: PhotoEntity?): PhotoEntity =
        if (old == null) {
            this
        } else {
            copy(
                urlRaw = old.urlRaw,
                urlFull = old.urlFull,
                urlRegular = old.urlRegular,
                urlSmall = old.urlSmall,
                urlThumb = old.urlThumb
            )
        }

    /** The photo joined or left the collection: its saved list of the user's collections follows. */
    suspend fun changePhotoMembership(photoId: String, collectionId: Int, isMember: Boolean) {
        val saved = photoDao.get(photoId) ?: return
        val ids = if (isMember) {
            (saved.currentUserCollectionIds + collectionId).distinct()
        } else {
            saved.currentUserCollectionIds - collectionId
        }
        photoDao.updateCollectionIds(id = photoId, collectionIds = ids)
    }

    /** Saves users from a list or a short form; a saved profile keeps its details and its current-user mark. */
    suspend fun saveUserSummaries(users: List<User>) {
        if (users.isEmpty()) return
        val saved = userDao.getByIds(users.map { it.id }).associateBy { it.id }
        userDao.upsertAll(
            users.map { user ->
                val old = saved[user.id]
                val summary = user.toEntity(isCurrent = old?.isCurrent ?: false, isDetailed = false)
                if (old != null && old.isDetailed) {
                    old.copy(
                        username = summary.username,
                        name = summary.name,
                        profileImage = summary.profileImage,
                        profileUrl = summary.profileUrl
                    )
                } else {
                    summary
                }
            }
        )
    }

    /** Saves a profile loaded by its own request. The email comes only for the current user, so it is not lost here. */
    suspend fun saveUser(user: User) {
        val old = userDao.getByUsername(user.username)
        userDao.upsert(
            user.copy(email = user.email ?: old?.email).toEntity(
                isCurrent = old?.isCurrent ?: false,
                isDetailed = true
            )
        )
    }

    /** Saves the signed-in user and marks it as the current one; the mark of any previous user is dropped after. */
    suspend fun saveCurrentUser(user: User) {
        userDao.upsert(user.toEntity(isCurrent = true, isDetailed = true))
        userDao.unmarkCurrentExcept(user.id)
    }

    suspend fun saveCollections(collections: List<PhotoCollection>) {
        if (collections.isEmpty()) return
        val saved = collectionDao.getByIds(collections.map { it.id }).associateBy { it.id }
        val changed = collections.map { it.toEntity() }.filter { entity -> entity != saved[entity.id] }
        if (changed.isNotEmpty()) collectionDao.upsertAll(changed)
    }

    suspend fun saveCollection(collection: PhotoCollection) {
        saveCollections(listOf(collection))
    }

    /** Saves a short list loaded in one request under [listKey], replacing what that list held. */
    suspend fun saveCollectionList(listKey: String, collections: List<PhotoCollection>) {
        saveCollections(collections)
        listDao.putEntries(
            listKey = listKey,
            itemIds = collections.map { it.id.toString() },
            startPosition = 0,
            isRefresh = true
        )
        listDao.upsertState(ListStateEntity(listKey = listKey, updatedAtEpochMillis = time.nowEpochMillis()))
    }

    suspend fun saveTopics(topics: List<Topic>) {
        if (topics.isEmpty()) return
        val saved = topicDao.getByIds(topics.map { it.id }).associateBy { it.id }
        val changed = topics.map { it.toEntity() }.filter { entity -> entity != saved[entity.id] }
        if (changed.isNotEmpty()) topicDao.upsertAll(changed)
    }

    suspend fun saveTopic(topic: Topic) {
        saveTopics(listOf(topic))
    }

    suspend fun saveStatistics(key: String, statistics: Statistics) {
        statsDao.upsertStatistics(statistics.toEntity(key))
    }

    /**
     * The lists and items whose key starts with [prefix] (`ListKeys`, `ItemKeys`) are loaded again the next time they
     * open; what they hold stays shown.
     */
    suspend fun invalidateLists(prefix: String) {
        listDao.deleteStatesByPrefix(prefix)
    }
}