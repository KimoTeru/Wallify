package org.kimoteru.wallify.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.database.entity.UserEntity

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(user: UserEntity)

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("SELECT * FROM user WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<UserEntity>

    @Query("SELECT * FROM user WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM user WHERE username = :username COLLATE NOCASE LIMIT 1")
    fun observeByUsername(username: String): Flow<UserEntity?>

    @Query("SELECT * FROM user WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrent(): Flow<UserEntity?>

    @Query("SELECT * FROM user WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrent(): UserEntity?

    /** Called after the new current user is saved, so the flow behind [observeCurrent] never goes empty in between. */
    @Query("UPDATE user SET isCurrent = 0 WHERE id != :id AND isCurrent = 1")
    suspend fun unmarkCurrentExcept(id: String)

    @Query("DELETE FROM user WHERE isCurrent = 1")
    suspend fun deleteCurrent()

    @Query(
        "SELECT user.* FROM list_entry INNER JOIN user ON user.id = list_entry.itemId " +
            "WHERE list_entry.listKey = :listKey ORDER BY list_entry.position ASC"
    )
    fun pagingSource(listKey: String): PagingSource<Int, UserEntity>

    /** Drops the users that no saved list holds any more; the signed-in user stays. */
    @Query("DELETE FROM user WHERE isCurrent = 0 AND id NOT IN (SELECT itemId FROM list_entry)")
    suspend fun deleteUnlisted()

    @Query("DELETE FROM user")
    suspend fun deleteAll()
}