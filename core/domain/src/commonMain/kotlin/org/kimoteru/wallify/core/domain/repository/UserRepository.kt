package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User

interface UserRepository {

    /**
     * The signed-in user. The session keeps it up to date: it is loaded when the token appears and cleared
     * on sign-out, so screens only read it and never load it. `null` until the first load and without a session.
     */
    val currentUser: StateFlow<User?>

    /** Loads the signed-in user and publishes it to [currentUser]. Needs the user token, otherwise 401. */
    suspend fun refreshCurrentUser(): Response<User>

    fun clearCurrentUser()

    /** Needs the user token with the write scope. Publishes the changed user to [currentUser]. */
    suspend fun updateCurrentUser(params: UpdateUserParams): Response<User>

    /** Behaves like `PhotoRepository.getPhotoFlow`: the database first, refreshed from the backend. */
    fun getUserFlow(username: String): Flow<Resource<User>>

    fun getUserPhotosFlow(
        username: String,
        order: UserPhotosOrder? = null,
        orientation: PhotoOrientation? = null
    ): Flow<PagingData<Photo>>

    fun getUserCollectionsFlow(username: String): Flow<PagingData<PhotoCollection>>

    /**
     * The first page of the user's collections (up to 30), for pickers that do not page. It is the head of the list
     * that [getUserCollectionsFlow] shows, so both follow the same saved collections.
     */
    fun getUserCollectionListFlow(username: String): Flow<Resource<List<PhotoCollection>>>

    /** [quantity] is the number of days, 1..30; `null` means the backend default. Behaves like [getUserFlow]. */
    fun getUserStatisticsFlow(username: String, quantity: Int? = null): Flow<Resource<Statistics>>
}