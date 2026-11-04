package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.model.collection.PhotoCollection
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User
import org.kimoteru.wallify.core.network.api.UsersApi
import org.kimoteru.wallify.core.network.model.request.user.toRequest
import org.kimoteru.wallify.core.network.model.response.collection.toDomain
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.kimoteru.wallify.core.network.model.response.stats.toDomain
import org.kimoteru.wallify.core.network.model.response.user.toDomain
import org.koin.core.annotation.Singleton

@Singleton
class UserRemoteDataSourceImpl(
    private val usersApi: UsersApi
) : UserRemoteDataSource {

    override suspend fun getCurrentUser(): Response<User> {
        return usersApi.getCurrentUser()
            .map { it.toDomain() }
    }

    override suspend fun updateCurrentUser(params: UpdateUserParams): Response<User> {
        return usersApi.updateCurrentUser(request = params.toRequest())
            .map { it.toDomain() }
    }

    override suspend fun getUser(username: String): Response<User> {
        return usersApi.getUser(username = username)
            .map { it.toDomain() }
    }

    override suspend fun getUserPhotos(
        username: String,
        page: Int,
        size: Int,
        order: UserPhotosOrder?,
        orientation: PhotoOrientation?
    ): Response<List<Photo>> {
        return usersApi.getUserPhotos(
            username = username,
            page = page,
            perPage = size,
            order = order,
            orientation = orientation
        ).map { photos -> photos.map { photo -> photo.toDomain() } }
    }

    override suspend fun getUserCollections(
        username: String,
        page: Int,
        size: Int
    ): Response<List<PhotoCollection>> {
        return usersApi.getUserCollections(username = username, page = page, perPage = size)
            .map { collections -> collections.map { collection -> collection.toDomain() } }
    }

    override suspend fun getUserStatistics(username: String, quantity: Int?): Response<Statistics> {
        return usersApi.getUserStatistics(username = username, quantity = quantity)
            .map { it.toDomain() }
    }
}