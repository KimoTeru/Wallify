package org.kimoteru.wallify.core.network.config

/** Kind of the running build. HTTP logging is enabled in debug builds only. */
interface AppBuild {
    val isDebug: Boolean
}