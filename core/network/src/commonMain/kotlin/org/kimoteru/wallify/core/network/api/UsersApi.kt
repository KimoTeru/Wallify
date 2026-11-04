package org.kimoteru.wallify.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.network.model.request.user.UpdateUserRequest
import org.kimoteru.wallify.core.network.model.response.collection.CollectionResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.stats.UserStatisticsResponse
import org.kimoteru.wallify.core.network.model.response.user.UserResponse
import org.kimoteru.wallify.core.network.response.executeRequest
import org.koin.core.annotation.Singleton

/** Endpoints for the current user (`me`) and for public users (`users`). */
@Singleton
class UsersApi(
    private val client: HttpClient
) {

    /** Needs the user token with the read scope; with the access key alone the backend answers 401. */
    suspend fun getCurrentUser(): Response<UserResponse> {
        return executeRequest {
            client.get("me")
        }
    }

    /** Needs the user token with the write scope. */
    suspend fun updateCurrentUser(request: UpdateUserRequest): Response<UserResponse> {
        return executeRequest {
            client.put("me") {
                setBody(
                    formContent(
                        "username" to request.username,
                        "first_name" to request.firstName,
                        "last_name" to request.lastName,
                        "email" to request.email,
                        "url" to request.url,
                        "location" to request.location,
                        "bio" to request.bio,
                        "instagram_username" to request.instagramUsername
                    )
                )
            }
        }
    }

    suspend fun getUser(username: String): Response<UserResponse> {
        return executeRequest {
            client.get("users/$username")
        }
    }

    /** Photos of the user. With [includeStatistics] every photo carries its statistics. */
    suspend fun getUserPhotos(
        username: String,
        page: Int,
        perPage: Int,
        order: UserPhotosOrder?,
        orientation: PhotoOrientation?,
        includeStatistics: Boolean = false,
        statisticsQuantity: Int? = null
    ): Response<List<PhotoResponse>> {
        return executeRequest {
            client.get("users/$username/photos") {
                pagination(page, perPage)
                parameter(QueryParam.ORDER_BY, order?.toQuery())
                parameter(QueryParam.ORIENTATION, orientation?.toQuery())
                if (includeStatistics) {
                    parameter(QueryParam.STATS, true)
                    statisticsRange(statisticsQuantity)
                }
            }
        }
    }

    suspend fun getUserCollections(username: String, page: Int, perPage: Int): Response<List<CollectionResponse>> {
        return executeRequest {
            client.get("users/$username/collections") { pagination(page, perPage) }
        }
    }

    suspend fun getUserStatistics(username: String, quantity: Int?): Response<UserStatisticsResponse> {
        return executeRequest {
            client.get("users/$username/statistics") { statisticsRange(quantity) }
        }
    }
}