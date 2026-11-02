package org.kimoteru.wallify.core.datastore.storage

expect class DataStorePathProvider {
    fun producePath(): String
}