package org.kimoteru.wallify.core.network.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.http.Parameters
import org.kimoteru.wallify.core.common.extensions.Comma
import org.kimoteru.wallify.core.model.photo.ContentFilter
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.PhotosOrder
import org.kimoteru.wallify.core.model.photo.SearchOrder
import org.kimoteru.wallify.core.model.photo.UserPhotosOrder
import org.kimoteru.wallify.core.model.topic.TopicsOrder

/** Query parameter names and fixed values of the backend API. */
internal object QueryParam {
    const val PAGE = "page"
    const val PER_PAGE = "per_page"
    const val ORDER_BY = "order_by"
    const val ORIENTATION = "orientation"
    const val CONTENT_FILTER = "content_filter"
    const val COLOR = "color"
    const val COLLECTIONS = "collections"
    const val TOPICS = "topics"
    const val IDS = "ids"
    const val USERNAME = "username"
    const val QUERY = "query"
    const val LANG = "lang"
    const val COUNT = "count"
    const val STATS = "stats"
    const val RESOLUTION = "resolution"
    const val QUANTITY = "quantity"
    const val PHOTO_ID = "photo_id"

    /** The only statistics frequency the backend supports. */
    const val RESOLUTION_DAYS = "days"

    /** Language of a search the backend uses when `lang` is absent; sending it would only need the beta access. */
    const val DEFAULT_LANGUAGE = "en"
}

internal fun HttpRequestBuilder.pagination(page: Int, perPage: Int) {
    parameter(QueryParam.PAGE, page)
    parameter(QueryParam.PER_PAGE, perPage)
}

internal fun HttpRequestBuilder.statisticsRange(quantity: Int?) {
    parameter(QueryParam.RESOLUTION, QueryParam.RESOLUTION_DAYS)
    parameter(QueryParam.QUANTITY, quantity)
}

/** Form body from [fields]; fields with a `null` value are left out. */
internal fun formContent(vararg fields: Pair<String, Any?>): FormDataContent = FormDataContent(
    Parameters.build {
        fields.forEach { (name, value) -> value?.let { append(name, it.toString()) } }
    }
)

/*
 * Query values of the backend. For every enum below the API value is the constant name in lower case.
 * Keep the enum names in sync with the backend when adding a value.
 */

internal fun PhotoOrientation.toQuery(): String = name.lowercase()

internal fun ContentFilter.toQuery(): String = name.lowercase()

internal fun PhotosOrder.toQuery(): String = name.lowercase()

internal fun UserPhotosOrder.toQuery(): String = name.lowercase()

internal fun SearchOrder.toQuery(): String = name.lowercase()

internal fun TopicsOrder.toQuery(): String = name.lowercase()

internal fun PhotoColor.toQuery(): String = when (this) {
    PhotoColor.BlackAndWhite -> "black_and_white"
    else -> name.lowercase()
}

/** Comma-separated list for a query parameter; an empty list means "no filter". */
internal fun <T> List<T>.toQueryList(): String? =
    takeIf { it.isNotEmpty() }?.joinToString(String.Comma)