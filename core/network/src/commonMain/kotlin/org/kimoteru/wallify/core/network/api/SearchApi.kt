package org.kimoteru.wallify.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.network.model.response.search.SearchCollectionsResponse
import org.kimoteru.wallify.core.network.model.response.search.SearchPhotosResponse
import org.kimoteru.wallify.core.network.model.response.search.SearchUsersResponse
import org.kimoteru.wallify.core.network.response.executeRequest
import org.koin.core.annotation.Singleton

/** Endpoints under `search`. Each call returns one page with the totals. */
@Singleton
class SearchApi(
    private val client: HttpClient
) {

    /**
     * [languageTag] is the language of the query; `null` and English leave the backend default and send nothing.
     * Non-English search is in the backend's beta and works only for applications it enabled.
     */
    suspend fun searchPhotos(
        query: SearchPhotosQuery,
        languageTag: String?,
        page: Int,
        perPage: Int
    ): Response<SearchPhotosResponse> {
        return executeRequest {
            client.get("search/photos") {
                parameter(QueryParam.QUERY, query.query)
                parameter(QueryParam.LANG, languageTag?.takeUnless { it == QueryParam.DEFAULT_LANGUAGE })
                pagination(page, perPage)
                parameter(QueryParam.ORDER_BY, query.order?.toQuery())
                parameter(QueryParam.COLLECTIONS, query.collectionIds.toQueryList())
                parameter(QueryParam.CONTENT_FILTER, query.contentFilter?.toQuery())
                parameter(QueryParam.COLOR, query.color?.toQuery())
                parameter(QueryParam.ORIENTATION, query.orientation?.toQuery())
            }
        }
    }

    suspend fun searchCollections(query: String, page: Int, perPage: Int): Response<SearchCollectionsResponse> {
        return executeRequest {
            client.get("search/collections") {
                parameter(QueryParam.QUERY, query)
                pagination(page, perPage)
            }
        }
    }

    suspend fun searchUsers(query: String, page: Int, perPage: Int): Response<SearchUsersResponse> {
        return executeRequest {
            client.get("search/users") {
                parameter(QueryParam.QUERY, query)
                pagination(page, perPage)
            }
        }
    }
}