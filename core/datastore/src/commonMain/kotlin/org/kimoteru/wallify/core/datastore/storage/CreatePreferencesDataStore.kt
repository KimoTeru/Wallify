package org.kimoteru.wallify.core.datastore.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val PREFERENCES_FILE_NAME = "wallify.preferences_pb"

internal fun createPreferencesDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath { path.toPath() }

internal fun createDataStore(pathProvider: DataStorePathProvider): DataStore<Preferences> =
    createPreferencesDataStore(pathProvider.producePath())