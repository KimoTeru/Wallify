package org.kimoteru.wallify.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.network.model.request.collection.CollectionRequest
import org.kimoteru.wallify.core.network.model.response.collection.CollectionPhotoChangeResponse
import org.kimoteru.wallify.core.network.model.response.collection.CollectionResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.response.executeRequest
import org.koin.core.annotation.Singleton

/** Endpoints under `collections`. Create, update, delete, add and remove need the user token with the write scope. */
@Singleton
class CollectionsApi(
    private val client: HttpClient
) {

    suspend fun getCollections(page: Int, perPage: Int): Response<List<CollectionResponse>> {
        return executeRequest {
            client.get("collections") { pagination(page, perPage) }
        }
    }

    suspend fun getCollection(id: Int): Response<CollectionResponse> {
        return executeRequest {
            client.get("collections/$id")
        }
    }

    suspend fun getCollectionPhotos(
        id: Int,
        page: Int,
        perPage: Int,
        orientation: PhotoOrientation?,
        order: PhotosOrder?
    ): Response<List<PhotoResponse>> {
        return executeRequest {
            client.get("collections/$id/photos") {
                pagination(page, perPage)
                parameter(QueryParam.ORIENTATION, orientation?.toQuery())
                parameter(QueryParam.ORDER_BY, order?.toQuery())
            }
        }
    }

    suspend fun getRelatedCollections(id: Int): Response<List<CollectionResponse>> {
        return executeRequest {
            client.get("collections/$id/related")
        }
    }

    suspend fun createCollection(request: CollectionRequest): Response<CollectionResponse> {
        return executeRequest {
            client.post("collections") { setBody(request.toForm()) }
        }
    }

    suspend fun updateCollection(id: Int, request: CollectionRequest): Response<CollectionResponse> {
        return executeRequest {
            client.put("collections/$id") { setBody(request.toForm()) }
        }
    }

    /** The backend answers 204 with an empty body. */
    suspend fun deleteCollection(id: Int): Response<Unit> {
        return executeRequest {
            client.delete("collections/$id")
        }
    }

    /** Adding a photo that is already in the collection has no effect. */
    suspend fun addPhoto(collectionId: Int, photoId: String): Response<CollectionPhotoChangeResponse> {
        return executeRequest {
            client.post("collections/$collectionId/add") {
                setBody(formContent(QueryParam.PHOTO_ID to photoId))
            }
        }
    }

    suspend fun removePhoto(collectionId: Int, photoId: String): Response<CollectionPhotoChangeResponse> {
        return executeRequest {
            client.delete("collections/$collectionId/remove") {
                parameter(QueryParam.PHOTO_ID, photoId)
            }
        }
    }

    private fun CollectionRequest.toForm() = formContent(
        "title" to title,
        "description" to description,
        "private" to isPrivate
    )
}