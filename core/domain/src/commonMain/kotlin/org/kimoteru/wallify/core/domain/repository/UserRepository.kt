package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User

interface UserRepository {

    /**
     * The signed-in user. The repository keeps it in step with the session itself: it is loaded when the token appears
     * and cleared on sign-out, so screens only read it and never load it. `null` until the first load and without a
     * session.
     */
    val currentUser: StateFlow<User?>

    /**
     * Why the repository could not load [currentUser]; `null` when the load went well or is not needed. A token the
     * backend rejects is not here: that ends the session instead. While the failure lasts, the load is repeated each
     * time the connection returns or [retryCurrentUserLoad] is called, and the value goes back to `null` on success.
     */
    val currentUserLoadError: StateFlow<ResponseError?>

    /** Loads the signed-in user again now (the user pressed «Повторить»); does nothing while there is no failure. */
    fun retryCurrentUserLoad()

    fun clearCurrentUser()

    /** Needs the user token with the write scope. Publishes the changed user to [currentUser]. */
    suspend fun updateCurrentUser(params: UpdateUserParams): Response<User>

    /**
     * Page of the photo service where the signed-in user uploads photos. The API cannot upload, so the screen opens
     * this address in the browser and, when the user is back, reloads the photos ([invalidateUserPhotos]).
     */
    val uploadPageUrl: String

    /** Behaves like `PhotoRepository.getPhotoFlow`: the database first, refreshed from the backend if not fresh. */
    fun getUserFlow(username: String): Flow<Resource<User>>

    fun getUserPhotosFlow(
        username: String,
        order: UserPhotosOrder? = null,
        orientation: PhotoOrientation? = null
    ): Flow<PagingData<Photo>>

    /**
     * Makes the saved photo lists of the user, the profile and its statistics stale: the next [getUserPhotosFlow],
     * [getUserFlow] and [getUserStatisticsFlow] collected load again, while the saved data stays on screen until the
     * new one arrives.
     */
    suspend fun invalidateUserPhotos(username: String)

    fun getUserCollectionsFlow(username: String): Flow<PagingData<PhotoCollection>>

    /**
     * The first page of the user's collections (up to 30), for pickers that do not page. It is the head of the list
     * that [getUserCollectionsFlow] shows, so both follow the same saved collections.
     */
    fun getUserCollectionListFlow(username: String): Flow<Resource<List<PhotoCollection>>>

    /** [quantity] is the number of days, 1..30; `null` means the backend default. Behaves like [getUserFlow]. */
    fun getUserStatisticsFlow(username: String, quantity: Int? = null): Flow<Resource<Statistics>>
}