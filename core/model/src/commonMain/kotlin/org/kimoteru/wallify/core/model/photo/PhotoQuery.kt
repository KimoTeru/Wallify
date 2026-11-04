package org.kimoteru.wallify.core.model.photo

enum class PhotoOrientation { Landscape, Portrait, Squarish }

/** `High` additionally removes content that may be unsuitable for younger audiences. */
enum class ContentFilter { Low, High }

enum class PhotoColor { BlackAndWhite, Black, White, Yellow, Orange, Red, Purple, Magenta, Green, Teal, Blue }

/** Order of photos of a collection or a topic. */
enum class PhotosOrder { Latest, Oldest, Popular }

enum class UserPhotosOrder { Latest, Oldest, Popular, Views, Downloads }

enum class SearchOrder { Relevant, Latest }

/**
 * Filters of a random photo. Every field is optional and they narrow the pool together,
 * except that [query] cannot be combined with [collectionIds] or [topics].
 * [topics] hold topic ids or slugs.
 */
data class RandomPhotoQuery(
    val collectionIds: List<Int> = emptyList(),
    val topics: List<String> = emptyList(),
    val username: String? = null,
    val query: String? = null,
    val orientation: PhotoOrientation? = null,
    val contentFilter: ContentFilter? = null
)

data class SearchPhotosQuery(
    val query: String,
    val order: SearchOrder? = null,
    val collectionIds: List<Int> = emptyList(),
    val contentFilter: ContentFilter? = null,
    val color: PhotoColor? = null,
    val orientation: PhotoOrientation? = null
)