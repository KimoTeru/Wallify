package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User

interface UserRepository {

    /** Needs the user token; with the access key alone the backend answers 401. */
    suspend fun getCurrentUser(): Response<User>

    /** Needs the user token with the write scope. */
    suspend fun updateCurrentUser(params: UpdateUserParams): Response<User>

    suspend fun getUser(username: String): Response<User>

    fun getUserPhotosFlow(
        username: String,
        order: UserPhotosOrder? = null,
        orientation: PhotoOrientation? = null
    ): Flow<PagingData<Photo>>

    fun getUserCollectionsFlow(username: String): Flow<PagingData<PhotoCollection>>

    /** [quantity] is the number of days, 1..30; `null` means the backend default. */
    suspend fun getUserStatistics(username: String, quantity: Int? = null): Response<Statistics>
}