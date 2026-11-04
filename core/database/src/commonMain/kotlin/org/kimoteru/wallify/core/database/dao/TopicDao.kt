package org.kimoteru.wallify.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.database.entity.TopicEntity

@Dao
interface TopicDao {
    @Upsert
    suspend fun upsert(topic: TopicEntity)

    @Upsert
    suspend fun upsertAll(topics: List<TopicEntity>)

    /** A topic is addressed by its id or its slug, the repository passes whichever the caller has. */
    @Query("SELECT * FROM topic WHERE id = :idOrSlug OR slug = :idOrSlug LIMIT 1")
    fun observeByIdOrSlug(idOrSlug: String): Flow<TopicEntity?>

    @Query(
        "SELECT topic.* FROM list_entry INNER JOIN topic ON topic.id = list_entry.itemId " +
            "WHERE list_entry.listKey = :listKey ORDER BY list_entry.position ASC"
    )
    fun pagingSource(listKey: String): PagingSource<Int, TopicEntity>

    @Query("DELETE FROM topic")
    suspend fun deleteAll()
}