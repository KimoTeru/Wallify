package org.kimoteru.wallify.core.data.cache

import kotlinx.coroutines.CancellationException
import org.kimoteru.wallify.core.database.dao.ListDao
import org.kimoteru.wallify.core.database.entity.ListEntryEntity
import org.kimoteru.wallify.core.platform.time.TimeProvider
import org.koin.core.annotation.Singleton
import kotlin.time.Duration

/**
 * What the paged lists share: the clock, the age check against a [CacheTtl] and the [ListDao]. The lists themselves
 * are loaded by [ListRemoteMediator] (see [pager]); single items and short lists by [refreshableFlow] and
 * [refreshableListFlow].
 */
@Singleton
class CacheLoader(
    internal val listDao: ListDao,
    private val time: TimeProvider
) {

    fun now(): Long = time.nowEpochMillis()

    /** A list saved at [savedAtEpochMillis] is fresh while it is younger than [ttl]; a zero [ttl] is never fresh. */
    fun isFresh(savedAtEpochMillis: Long, ttl: Duration): Boolean {
        if (ttl <= Duration.ZERO) return false
        val age = now() - savedAtEpochMillis
        return age in 0 until ttl.inWholeMilliseconds
    }

    /** A failing write must not turn a good answer of the backend into an error. */
    internal suspend fun saveQuietly(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // The answer is still correct, it is just not saved; the next request saves it.
        }
    }
}

/**
 * Writes one loaded range of the list [listKey] from [startPosition]. After a refresh ([isRefresh]) the old tail of
 * the list is dropped, but the first rows are replaced in place, so the list on screen is never empty in between.
 */
internal suspend fun ListDao.putEntries(
    listKey: String,
    itemIds: List<String>,
    startPosition: Int,
    isRefresh: Boolean
) {
    insertEntries(
        itemIds.mapIndexed { index, itemId ->
            ListEntryEntity(listKey = listKey, position = startPosition + index, itemId = itemId)
        }
    )
    if (isRefresh) deleteEntriesFrom(listKey = listKey, fromPosition = startPosition + itemIds.size)
}