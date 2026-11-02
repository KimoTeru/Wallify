package org.kimoteru.wallify.core.common.functional

inline fun <T> runIfOrNull(condition: Boolean, block: () -> T): T? {
    return if (condition) block() else null
}