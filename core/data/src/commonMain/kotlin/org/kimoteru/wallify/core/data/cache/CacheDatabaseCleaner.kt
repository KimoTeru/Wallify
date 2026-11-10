package org.kimoteru.wallify.core.data.cache

import kotlinx.coroutines.CancellationException
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.dao.TopicDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.kimoteru.wallify.core.platform.time.TimeProvider
import org.koin.core.annotation.Singleton

/**
 * Empties every table of the database. Room KMP has no `clearAllTables`, so each table is listed here:
 * a new table must be added to [clearAll], otherwise a signed-out user's data would survive in it.
 *
 * The order drops the lists first, so a screen that is still reading never meets entries without their items.
 *
 * It also keeps the database from growing for ever: [pruneStale] drops what was not loaded for [CacheTtl.Retention].
 */
@Singleton
class CacheDatabaseCleaner(
    private val listDao: ListDao,
    private val photoDao: PhotoDao,
    private val userDao: UserDao,
    private val collectionDao: CollectionDao,
    private val topicDao: TopicDao,
    private val statsDao: StatsDao,
    private val time: TimeProvider
) {

    /**
     * Removes the lists and items not loaded for [CacheTtl.Retention] (old searches, profiles seen once), then the
     * photos, users, collections, topics and statistics that no remaining list or item refers to. Run it while no
     * screen reads the database (at the start of the app). The signed-in user and the counters stay.
     */
    suspend fun pruneStale() {
        val threshold = time.nowEpochMillis() - CacheTtl.Retention.inWholeMilliseconds
        listDao.deleteEntriesOfStatesBefore(threshold)
        listDao.deleteStatesBefore(threshold)
        photoDao.deleteUnlisted()
        userDao.deleteUnlisted()
        collectionDao.deleteUnlisted()
        topicDao.deleteUnlisted()
        statsDao.deleteStatisticsWithoutState()
    }

    /** Runs every step even if one fails, and rethrows the first failure after the last step. */
    suspend fun clearAll() {
        val steps: List<suspend () -> Unit> = listOf(
            { listDao.deleteAllEntries() },
            { listDao.deleteAllStates() },
            { photoDao.deleteAll() },
            { userDao.deleteAll() },
            { collectionDao.deleteAll() },
            { topicDao.deleteAll() },
            { statsDao.deleteAllStatistics() },
            { statsDao.deleteTotal() },
            { statsDao.deleteMonth() }
        )
        var failure: Exception? = null
        steps.forEach { step ->
            try {
                step()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                if (failure == null) failure = exception
            }
        }

        failure?.let { throw it }
    }
}