package org.kimoteru.wallify.core.data.stats

import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.json.Json
import org.kimoteru.wallify.core.datastore.prefs.DataStorePrefs
import org.kimoteru.wallify.core.model.stats.RequestStats
import org.koin.core.annotation.Singleton

/** Keeps the counters as one JSON value; the rate limit is not saved, it is stale after an hour. */
@Singleton
class DataStoreRequestStatsDataSource(
    private val prefs: DataStorePrefs,
    private val json: Json,
) : RequestStatsLocalDataSource {
    override suspend fun read(): RequestStats {
        val raw = prefs.getNullable(key)
        if (raw.isNullOrBlank()) return RequestStats.Empty
        return json.decodeFromString(RequestStats.serializer(), raw)
    }

    override suspend fun write(stats: RequestStats) {
        prefs.save(key, json.encodeToString(RequestStats.serializer(), stats.copy(rateLimit = null)))
    }

    private companion object {
        val key = stringPreferencesKey("request_stats")
    }
}