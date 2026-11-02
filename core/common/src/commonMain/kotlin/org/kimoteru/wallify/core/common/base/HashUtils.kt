package org.kimoteru.wallify.core.common.base

/**
 * Creates a stable hash based on the class name.
 *
 * Prevents collisions where different navigation argument data classes
 * could produce the same `hashCode()` if their field values are identical.
 */
inline fun <reified T> getNameHash(): Int {
    val name = T::class.simpleName ?: "Unknown"
    return name.hashCode() * 31
}