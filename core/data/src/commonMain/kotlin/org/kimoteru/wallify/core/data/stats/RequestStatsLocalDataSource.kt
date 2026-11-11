package org.kimoteru.wallify.core.data.stats

import org.kimoteru.wallify.core.model.stats.RequestStats

interface RequestStatsLocalDataSource {
    suspend fun read(): RequestStats

    suspend fun write(stats: RequestStats)
}