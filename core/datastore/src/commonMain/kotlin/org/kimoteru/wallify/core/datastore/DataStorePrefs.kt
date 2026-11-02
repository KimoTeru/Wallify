package org.kimoteru.wallify.core.datastore

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface DataStorePrefs {
    fun <T> getFlow(key: Preferences.Key<T>, defValue: T): Flow<T>
    fun <T> getNullableFlow(key: Preferences.Key<T>): Flow<T?>
    suspend fun <T> get(key: Preferences.Key<T>, defValue: T): T
    suspend fun <T> getNullable(key: Preferences.Key<T>): T?
    suspend fun <T> save(key: Preferences.Key<T>, value: T)
    suspend fun <T> remove(key: Preferences.Key<T>)
}