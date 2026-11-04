package org.kimoteru.wallify.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.topic.TopicsOrder
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.topic.TopicResponse
import org.kimoteru.wallify.core.network.response.executeRequest
import org.koin.core.annotation.Singleton

/** Endpoints under `topics`. A topic is addressed by its id or slug. */
@Singleton
class TopicsApi(
    private val client: HttpClient
) {

    suspend fun getTopics(
        page: Int,
        perPage: Int,
        ids: List<String>,
        order: TopicsOrder?
    ): Response<List<TopicResponse>> {
        return executeRequest {
            client.get("topics") {
                pagination(page, perPage)
                parameter(QueryParam.IDS, ids.toQueryList())
                parameter(QueryParam.ORDER_BY, order?.toQuery())
            }
        }
    }

    suspend fun getTopic(idOrSlug: String): Response<TopicResponse> {
        return executeRequest {
            client.get("topics/$idOrSlug")
        }
    }

    suspend fun getTopicPhotos(
        idOrSlug: String,
        page: Int,
        perPage: Int,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Response<List<PhotoResponse>> {
        return executeRequest {
            client.get("topics/$idOrSlug/photos") {
                pagination(page, perPage)
                parameter(QueryParam.ORIENTATION, orientation?.toQuery())
                parameter(QueryParam.ORDER_BY, order?.toQuery())
            }
        }
    }
}