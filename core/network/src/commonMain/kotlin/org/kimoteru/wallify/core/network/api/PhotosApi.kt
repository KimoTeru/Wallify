package org.kimoteru.wallify.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.network.model.request.photo.UpdatePhotoRequest
import org.kimoteru.wallify.core.network.model.response.photo.PhotoDownloadResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.stats.PhotoStatisticsResponse
import org.kimoteru.wallify.core.network.response.executeRequest
import org.koin.core.annotation.Singleton

/** Endpoints under `photos`. */
@Singleton
class PhotosApi(
    private val client: HttpClient
) {

    /** Editorial feed, one page. */
    suspend fun getPhotos(page: Int, perPage: Int): Response<List<PhotoResponse>> {
        return executeRequest {
            client.get("photos") { pagination(page, perPage) }
        }
    }

    suspend fun getPhoto(id: String): Response<PhotoResponse> {
        return executeRequest {
            client.get("photos/$id")
        }
    }

    /** One random photo. The backend does not allow `collections` / `topics` together with `query`. */
    suspend fun getRandomPhoto(query: RandomPhotoQuery): Response<PhotoResponse> {
        return executeRequest {
            client.get("photos/random") { randomFilters(query) }
        }
    }

    /** Several random photos: with `count` the backend answers with an array even for one photo. */
    suspend fun getRandomPhotos(count: Int, query: RandomPhotoQuery): Response<List<PhotoResponse>> {
        return executeRequest {
            client.get("photos/random") {
                parameter(QueryParam.COUNT, count)
                randomFilters(query)
            }
        }
    }

    suspend fun getPhotoStatistics(id: String, quantity: Int?): Response<PhotoStatisticsResponse> {
        return executeRequest {
            client.get("photos/$id/statistics") { statisticsRange(quantity) }
        }
    }

    /** Must be called every time the app downloads a photo. The answer holds the file URL. */
    suspend fun trackDownload(id: String): Response<PhotoDownloadResponse> {
        return executeRequest {
            client.get("photos/$id/download")
        }
    }

    /** Needs the user token with the photo write scope. */
    suspend fun updatePhoto(id: String, request: UpdatePhotoRequest): Response<PhotoResponse> {
        return executeRequest {
            client.put("photos/$id") {
                setBody(
                    formContent(
                        "description" to request.description,
                        "show_on_profile" to request.showOnProfile,
                        "tags" to request.tags,
                        "location[latitude]" to request.location?.latitude,
                        "location[longitude]" to request.location?.longitude,
                        "location[name]" to request.location?.name,
                        "location[city]" to request.location?.city,
                        "location[country]" to request.location?.country,
                        "exif[make]" to request.exif?.make,
                        "exif[model]" to request.exif?.model,
                        "exif[exposure_time]" to request.exif?.exposureTime,
                        "exif[aperture_value]" to request.exif?.apertureValue,
                        "exif[focal_length]" to request.exif?.focalLength,
                        "exif[iso_speed_ratings]" to request.exif?.isoSpeedRatings
                    )
                )
            }
        }
    }

    private fun HttpRequestBuilder.randomFilters(query: RandomPhotoQuery) {
        parameter(QueryParam.COLLECTIONS, query.collectionIds.toQueryList())
        parameter(QueryParam.TOPICS, query.topics.toQueryList())
        parameter(QueryParam.USERNAME, query.username)
        parameter(QueryParam.QUERY, query.query)
        parameter(QueryParam.ORIENTATION, query.orientation?.toQuery())
        parameter(QueryParam.CONTENT_FILTER, query.contentFilter?.toQuery())
    }
}