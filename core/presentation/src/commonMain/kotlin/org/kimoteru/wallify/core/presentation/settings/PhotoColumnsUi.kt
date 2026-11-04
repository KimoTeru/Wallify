package org.kimoteru.wallify.core.presentation.settings

/** How many columns a photo grid uses. [count] is null when the window width decides. */
enum class PhotoColumnsUi(val count: Int?) {
    Auto(null),
    One(1),
    Two(2),
    Three(3),
    Four(4)
}

fun PhotoColumnsUi.toDomain(): Int? = count

fun Int?.toPhotoColumns(): PhotoColumnsUi = PhotoColumnsUi.entries.first { columns -> columns.count == this }