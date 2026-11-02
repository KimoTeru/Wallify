package org.kimoteru.wallify.core.common.extensions

data class DiffListResult<T>(
    val added: List<T>,
    val updated: List<T>,
    val removed: List<T>
)

fun <T> List<T>.diffById(
    newList: List<T>,
    getId: (T) -> Long
): DiffListResult<T> {
    val oldMap = this.associateBy(getId)
    val newMap = newList.associateBy(getId)

    val added = newList.filter { getId(it) !in oldMap }
    val updated = newList.filter { item ->
        val id = getId(item)
        val existing = oldMap[id]
        existing != null && existing != item
    }
    val removed = (oldMap.keys - newMap.keys)
        .mapNotNull { oldMap[it] }

    return DiffListResult(added, updated, removed)
}

fun <T> List<T>?.listsEqualById(
    listsEqual: List<T>?, idSelector: (T) -> Any
): Boolean {
    if (this == null && listsEqual == null) return true
    if (this == null || listsEqual == null) return false
    return this.size == listsEqual.size && this.map(idSelector).toSet() == listsEqual.map(idSelector).toSet()
}