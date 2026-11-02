package org.kimoteru.wallify.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.kimoteru.wallify.core.datastore.storage.DataStorePathProvider
import org.kimoteru.wallify.core.datastore.storage.createDataStore
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.core.datastore")
class DataStoreModule {
    @Singleton
    fun preferencesDataStore(pathProvider: DataStorePathProvider): DataStore<Preferences> =
        createDataStore(pathProvider)
}