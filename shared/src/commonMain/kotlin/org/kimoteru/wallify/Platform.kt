package org.kimoteru.wallify

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform