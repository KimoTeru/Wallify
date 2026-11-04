package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User

interface UserRemoteDataSource {

    suspend fun getCurrentUser(): Response<User>

    suspend fun updateCurrentUser(params: UpdateUserParams): Response<User>

    suspend fun getUser(username: String): Response<User>

    suspend fun getUserPhotos(
        username: String,
        page: Int,
        size: Int,
        order: UserPhotosOrder?,
        orientation: PhotoOrientation?
    ): Response<List<Photo>>

    suspend fun getUserCollections(username: String, page: Int, size: Int): Response<List<PhotoCollection>>

    suspend fun getUserStatistics(username: String, quantity: Int?): Response<Statistics>
}