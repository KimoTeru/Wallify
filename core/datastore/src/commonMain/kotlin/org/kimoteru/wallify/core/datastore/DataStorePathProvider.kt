package org.kimoteru.wallify.core.datastore

expect class DataStorePathProvider {
    fun producePath(): String
}