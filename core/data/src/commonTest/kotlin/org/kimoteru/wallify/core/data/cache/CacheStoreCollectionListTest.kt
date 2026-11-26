package org.kimoteru.wallify.core.data.cache

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.kimoteru.wallify.core.common.paging.DEFAULT_PAGE_SIZE
import org.kimoteru.wallify.core.database.dao.CollectionDao
import org.kimoteru.wallify.core.database.dao.PhotoDao
import org.kimoteru.wallify.core.database.dao.StatsDao
import org.kimoteru.wallify.core.database.dao.TopicDao
import org.kimoteru.wallify.core.database.dao.UserDao
import org.kimoteru.wallify.core.database.entity.CollectionEntity
import org.kimoteru.wallify.core.database.entity.PhotoEntity
import org.kimoteru.wallify.core.database.entity.StatisticsEntity
import org.kimoteru.wallify.core.database.entity.TopicEntity
import org.kimoteru.wallify.core.database.entity.UserEntity
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CacheStoreCollectionListTest {

    private val listDao = FakeListDao()
    private val store = CacheStore(
        photoDao = UnusedPhotoDao(),
        userDao = UnusedUserDao(),
        collectionDao = FakeCollectionDao(),
        topicDao = UnusedTopicDao(),
        statsDao = UnusedStatsDao(),
        listDao = listDao,
        time = FakeTimeProvider(now = 1_000L)
    )

    @Test
    fun `list shorter than a page is marked as ended`() = runTest {
        store.saveCollectionList(LIST_KEY, collections(DEFAULT_PAGE_SIZE - 1))

        assertTrue(listDao.isEnded(LIST_KEY) == true)
    }

    @Test
    fun `empty list is marked as ended`() = runTest {
        store.saveCollectionList(LIST_KEY, emptyList())

        assertTrue(listDao.isEnded(LIST_KEY) == true)
    }

    @Test
    fun `full page is not marked as ended so the next page can be asked`() = runTest {
        store.saveCollectionList(LIST_KEY, collections(DEFAULT_PAGE_SIZE))

        assertFalse(listDao.isEnded(LIST_KEY) == true)
        assertEquals(DEFAULT_PAGE_SIZE, listDao.countEntries(LIST_KEY))
    }

    private fun collections(count: Int) = List(count) { id ->
        PhotoCollection(
            id = id,
            title = "title $id",
            description = null,
            totalPhotos = 0,
            isPrivate = false,
            coverPhoto = null,
            user = null
        )
    }

    private companion object {
        const val LIST_KEY = "collections:user:test"
    }
}

private class FakeCollectionDao : CollectionDao {
    private val saved = mutableMapOf<Int, CollectionEntity>()

    override suspend fun upsert(collection: CollectionEntity) {
        saved[collection.id] = collection
    }

    override suspend fun upsertAll(collections: List<CollectionEntity>) {
        collections.forEach { saved[it.id] = it }
    }

    override suspend fun getByIds(ids: List<Int>): List<CollectionEntity> = ids.mapNotNull { saved[it] }

    override fun observe(id: Int): Flow<CollectionEntity?> = error(UNUSED)

    override fun observeList(listKey: String, limit: Int): Flow<List<CollectionEntity>> = error(UNUSED)

    override fun pagingSource(listKey: String): PagingSource<Int, CollectionEntity> = error(UNUSED)

    override suspend fun delete(id: Int) = error(UNUSED)

    override suspend fun deleteUnlisted() = error(UNUSED)

    override suspend fun deleteAll() = error(UNUSED)
}

private class UnusedPhotoDao : PhotoDao {
    override suspend fun upsertAll(photos: List<PhotoEntity>) = error(UNUSED)
    override suspend fun get(id: String): PhotoEntity? = error(UNUSED)
    override suspend fun getByIds(ids: List<String>): List<PhotoEntity> = error(UNUSED)
    override suspend fun getList(listKey: String, limit: Int): List<PhotoEntity> = error(UNUSED)
    override fun observe(id: String): Flow<PhotoEntity?> = error(UNUSED)
    override fun pagingSource(listKey: String): PagingSource<Int, PhotoEntity> = error(UNUSED)
    override suspend fun updateCollectionIds(id: String, collectionIds: List<Int>) = error(UNUSED)
    override suspend fun deleteUnlisted() = error(UNUSED)
    override suspend fun deleteAll() = error(UNUSED)
}

private class UnusedUserDao : UserDao {
    override suspend fun upsert(user: UserEntity) = error(UNUSED)
    override suspend fun upsertAll(users: List<UserEntity>) = error(UNUSED)
    override suspend fun getByUsername(username: String): UserEntity? = error(UNUSED)
    override suspend fun getByIds(ids: List<String>): List<UserEntity> = error(UNUSED)
    override suspend fun getCurrent(): UserEntity? = error(UNUSED)
    override fun observeByUsername(username: String): Flow<UserEntity?> = error(UNUSED)
    override fun observeCurrent(): Flow<UserEntity?> = error(UNUSED)
    override fun pagingSource(listKey: String): PagingSource<Int, UserEntity> = error(UNUSED)
    override suspend fun unmarkCurrentExcept(id: String) = error(UNUSED)
    override suspend fun deleteCurrent() = error(UNUSED)
    override suspend fun deleteUnlisted() = error(UNUSED)
    override suspend fun deleteAll() = error(UNUSED)
}

private class UnusedTopicDao : TopicDao {
    override suspend fun upsert(topic: TopicEntity) = error(UNUSED)
    override suspend fun upsertAll(topics: List<TopicEntity>) = error(UNUSED)
    override suspend fun getByIds(ids: List<String>): List<TopicEntity> = error(UNUSED)
    override fun observeByIdOrSlug(idOrSlug: String): Flow<TopicEntity?> = error(UNUSED)
    override fun pagingSource(listKey: String): PagingSource<Int, TopicEntity> = error(UNUSED)
    override suspend fun deleteUnlisted() = error(UNUSED)
    override suspend fun deleteAll() = error(UNUSED)
}

private class UnusedStatsDao : StatsDao {
    override suspend fun upsertStatistics(statistics: StatisticsEntity) = error(UNUSED)
    override fun observeStatistics(key: String): Flow<StatisticsEntity?> = error(UNUSED)
    override suspend fun deleteStatisticsWithoutState() = error(UNUSED)
    override suspend fun deleteAllStatistics() = error(UNUSED)
}

private const val UNUSED = "not used by the test"