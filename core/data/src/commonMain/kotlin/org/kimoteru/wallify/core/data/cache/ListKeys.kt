package org.kimoteru.wallify.core.data.cache

import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.topic.TopicsOrder

/**
 * Keys of the saved lists (`list_entry.listKey`, `list_state.listKey`). A key says what the list holds and with
 * which parameters, so two screens with the same parameters share one list and different parameters never mix.
 * Every key starts with the kind of the list, which lets a write drop the freshness of a whole kind
 * (see [COLLECTIONS_PREFIX]).
 */
internal object ListKeys {

    /** All lists whose items are collections or depend on them: collections of everyone and of a user, their photos. */
    const val COLLECTIONS_PREFIX = "collections:"

    /** Lists of the photos of one user. */
    const val USER_PHOTOS_PREFIX = "users:photos:"

    const val PHOTOS_FEED = "photos:feed"
    const val COLLECTIONS_FEED = "collections:feed"

    fun randomPhotos(count: Int, query: RandomPhotoQuery): String =
        "photos:random:$count:" + listOf(
            query.collectionIds.joinToString(","),
            query.topics.joinToString(","),
            query.username.orEmpty(),
            query.query.orEmpty(),
            query.orientation?.name.orEmpty(),
            query.contentFilter?.name.orEmpty()
        ).joinToString("|")

    fun collectionPhotos(id: Int, orientation: PhotoOrientation?, order: PhotosOrder?): String =
        "collections:photos:$id:${orientation?.name.orEmpty()}:${order?.name.orEmpty()}"

    fun relatedCollections(id: Int): String = "collections:related:$id"

    fun userCollections(username: String): String = "collections:user:${username.lowercase()}"

    fun topics(ids: List<String>, order: TopicsOrder?): String =
        "topics:feed:${ids.joinToString(",")}:${order?.name.orEmpty()}"

    fun topicPhotos(idOrSlug: String, orientation: PhotoOrientation?, order: PhotosOrder?): String =
        "topics:photos:$idOrSlug:${orientation?.name.orEmpty()}:${order?.name.orEmpty()}"

    fun userPhotos(username: String, order: UserPhotosOrder?, orientation: PhotoOrientation?): String =
        "$USER_PHOTOS_PREFIX${username.lowercase()}:${order?.name.orEmpty()}:${orientation?.name.orEmpty()}"

    fun searchPhotos(query: SearchPhotosQuery): String =
        "search:photos:" + listOf(
            query.query.trim().lowercase(),
            query.order?.name.orEmpty(),
            query.collectionIds.joinToString(","),
            query.contentFilter?.name.orEmpty(),
            query.color?.name.orEmpty(),
            query.orientation?.name.orEmpty()
        ).joinToString("|")

    fun searchCollections(query: String): String = "search:collections:${query.trim().lowercase()}"

    fun searchUsers(query: String): String = "search:users:${query.trim().lowercase()}"
}

/** Keys of the saved statistics rows. */
internal object StatisticsKeys {
    fun photo(id: String, quantity: Int?): String = "photo:$id:${quantity ?: 0}"

    fun user(username: String, quantity: Int?): String = "user:${username.lowercase()}:${quantity ?: 0}"
}