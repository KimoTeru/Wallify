package org.kimoteru.wallify.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Singleton

@Singleton
class DataStorePrefsImpl(
    private val dataStore: DataStore<Preferences>
) : DataStorePrefs {
    override fun <T> getFlow(key: Preferences.Key<T>, defValue: T): Flow<T> =
        dataStore.data
            .map { preferences -> preferences[key] ?: defValue }
            .distinctUntilChanged()

    override fun <T> getNullableFlow(key: Preferences.Key<T>): Flow<T?> =
        dataStore.data
            .map { preferences -> preferences[key] }
            .distinctUntilChanged()

    override suspend fun <T> get(key: Preferences.Key<T>, defValue: T): T =
        dataStore.data.first()[key] ?: defValue

    override suspend fun <T> getNullable(key: Preferences.Key<T>): T? =
        dataStore.data.first()[key]

    override suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    override suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}