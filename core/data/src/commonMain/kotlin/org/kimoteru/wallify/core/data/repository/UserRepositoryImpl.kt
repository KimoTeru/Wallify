package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kimoteru.wallify.core.common.paging.DEFAULT_PAGE_SIZE
import org.kimoteru.wallify.core.common.paging.FIRST_PAGE
import org.kimoteru.wallify.core.common.paging.createPager
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.onSuccess
import org.kimoteru.wallify.core.data.remote.UserRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.UserRepository
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User
import org.koin.core.annotation.Singleton

@Singleton
class UserRepositoryImpl(
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override val currentUser: StateFlow<User?>
        field = MutableStateFlow<User?>(null)

    override suspend fun refreshCurrentUser(): Response<User> {
        return userRemoteDataSource.getCurrentUser()
            .onSuccess { user -> currentUser.value = user }
    }

    override fun clearCurrentUser() {
        currentUser.value = null
    }

    override suspend fun updateCurrentUser(params: UpdateUserParams): Response<User> {
        return userRemoteDataSource.updateCurrentUser(params = params)
            .onSuccess { user -> currentUser.value = user }
    }

    override suspend fun getUser(username: String): Response<User> {
        return userRemoteDataSource.getUser(username = username)
    }

    override fun getUserPhotosFlow(
        username: String,
        order: UserPhotosOrder?,
        orientation: PhotoOrientation?
    ): Flow<PagingData<Photo>> {
        return createPager { page, loadSize ->
            userRemoteDataSource.getUserPhotos(
                username = username,
                page = page,
                size = loadSize,
                order = order,
                orientation = orientation
            )
        }
    }

    override fun getUserCollectionsFlow(username: String): Flow<PagingData<PhotoCollection>> {
        return createPager { page, loadSize ->
            userRemoteDataSource.getUserCollections(username = username, page = page, size = loadSize)
        }
    }

    override suspend fun getUserCollections(username: String): Response<List<PhotoCollection>> {
        return userRemoteDataSource.getUserCollections(
            username = username,
            page = FIRST_PAGE,
            size = DEFAULT_PAGE_SIZE
        )
    }

    override suspend fun getUserStatistics(username: String, quantity: Int?): Response<Statistics> {
        return userRemoteDataSource.getUserStatistics(username = username, quantity = quantity)
    }
}