package org.kimoteru.wallify.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.SearchPhotosQuery
import org.kimoteru.wallify.core.model.topic.TopicsOrder
import org.kimoteru.wallify.core.network.api.CollectionsApi
import org.kimoteru.wallify.core.network.api.PhotosApi
import org.kimoteru.wallify.core.network.api.SearchApi
import org.kimoteru.wallify.core.network.api.StatsApi
import org.kimoteru.wallify.core.network.api.TopicsApi
import org.kimoteru.wallify.core.network.api.UsersApi
import org.kimoteru.wallify.core.network.client.HttpClientFactory
import org.kimoteru.wallify.core.network.config.StaticNetworkConfig
import org.kimoteru.wallify.core.network.headers.HeadersCollectorRepositoryImpl
import org.kimoteru.wallify.core.network.model.request.collection.CollectionRequest
import org.kimoteru.wallify.core.network.model.request.user.UpdateUserRequest
import org.kimoteru.wallify.core.network.model.response.collection.CollectionPhotoChangeResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoDownloadResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.search.SearchPhotosResponse
import org.kimoteru.wallify.core.network.model.response.stats.TotalStatsResponse
import org.kimoteru.wallify.core.network.model.response.topic.TopicResponse
import org.kimoteru.wallify.core.network.session.SessionExpiryCollectorImpl
import org.kimoteru.wallify.core.network.stats.RequestStatsCollectorImpl

class ApiEndpointsTest {

    @Test
    fun photoListSendsPaginationAndDecodesSummary() = runBlocking {
        val client = client { request ->
            assertEquals("/photos", request.url.encodedPath)
            assertEquals("2", request.url.parameters["page"])
            assertEquals("30", request.url.parameters["per_page"])
            json("[$PHOTO]")
        }

        val photo = assertIs<Response.Success<List<PhotoResponse>>>(
            PhotosApi(client).getPhotos(page = 2, perPage = 30)
        ).data.single()

        assertEquals("LBI7cgq3pbM", photo.id)
        assertEquals("#60544D", photo.color)
        assertEquals("poorkane", photo.user?.username)
        assertEquals("https://images.unsplash.com/regular", photo.urls.regular)
        assertEquals(206, photo.currentUserCollections.single().id)
        assertNull(photo.currentUserCollections.single().coverPhoto)
        assertEquals("https://api.unsplash.com/photos/LBI7cgq3pbM/download", photo.links?.downloadLocation)
        client.close()
    }

    @Test
    fun randomWithCountAsksForArrayAndPassesFilters() = runBlocking {
        val client = client { request ->
            assertEquals("/photos/random", request.url.encodedPath)
            assertEquals("3", request.url.parameters["count"])
            assertEquals("landscape", request.url.parameters["orientation"])
            assertNull(request.url.parameters["query"])
            json("[$PHOTO]")
        }

        val result = PhotosApi(client).getRandomPhotos(
            count = 3,
            query = RandomPhotoQuery(orientation = PhotoOrientation.Landscape)
        )

        assertIs<Response.Success<*>>(result)
        client.close()
    }

    @Test
    fun trackDownloadReturnsFileUrl() = runBlocking {
        val client = client { request ->
            assertEquals("/photos/p1/download", request.url.encodedPath)
            json("""{"url":"https://images.unsplash.com/example"}""")
        }

        val download = assertIs<Response.Success<PhotoDownloadResponse>>(PhotosApi(client).trackDownload("p1")).data

        assertEquals("https://images.unsplash.com/example", download.url)
        client.close()
    }

    @Test
    fun searchDecodesTotalsAndResults() = runBlocking {
        val client = client { request ->
            assertEquals("/search/photos", request.url.encodedPath)
            assertEquals("office", request.url.parameters["query"])
            assertEquals("black_and_white", request.url.parameters["color"])
            assertEquals("ru", request.url.parameters["lang"])
            json("""{"total":133,"total_pages":7,"results":[$PHOTO]}""")
        }

        val page = assertIs<Response.Success<SearchPhotosResponse>>(
            SearchApi(client).searchPhotos(
                query = SearchPhotosQuery(query = "office", color = PhotoColor.BlackAndWhite),
                languageTag = "ru",
                page = 1,
                perPage = 10
            )
        ).data

        assertEquals(133, page.total)
        assertEquals(7, page.totalPages)
        assertEquals(1, page.results.size)
        client.close()
    }

    @Test
    fun userPhotosWithStatisticsAddRange() = runBlocking {
        val client = client { request ->
            assertEquals("/users/jimmy/photos", request.url.encodedPath)
            assertEquals("true", request.url.parameters["stats"])
            assertEquals("days", request.url.parameters["resolution"])
            assertEquals("7", request.url.parameters["quantity"])
            json("[]")
        }

        val result = UsersApi(client).getUserPhotos(
            username = "jimmy",
            page = 1,
            perPage = 10,
            order = null,
            orientation = null,
            includeStatistics = true,
            statisticsQuantity = 7
        )

        assertIs<Response.Success<*>>(result)
        client.close()
    }

    @Test
    fun updateCurrentUserSendsOnlyFilledFields() = runBlocking {
        val client = client { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("/me", request.url.encodedPath)
            val form = assertIs<FormDataContent>(request.body).formData
            assertEquals("Jim", form["first_name"])
            assertEquals("jim.example", form["instagram_username"])
            assertNull(form["email"])
            json("""{"id":"u1","username":"jimmy"}""")
        }

        val result = UsersApi(client).updateCurrentUser(
            UpdateUserRequest(firstName = "Jim", instagramUsername = "jim.example")
        )

        assertIs<Response.Success<*>>(result)
        client.close()
    }

    @Test
    fun addPhotoPostsPhotoIdAsForm() = runBlocking {
        val client = client { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/collections/298/add", request.url.encodedPath)
            assertEquals("p1", assertIs<FormDataContent>(request.body).formData["photo_id"])
            respond(
                content = """{"photo":$PHOTO,"collection":{"id":298,"title":"API test"}}""",
                status = HttpStatusCode.Created,
                headers = jsonHeaders()
            )
        }

        val change = assertIs<Response.Success<CollectionPhotoChangeResponse>>(
            CollectionsApi(client).addPhoto(collectionId = 298, photoId = "p1")
        ).data

        assertEquals("API test", change.collection.title)
        client.close()
    }

    @Test
    fun removePhotoSendsPhotoIdInQuery() = runBlocking {
        val client = client { request ->
            assertEquals(HttpMethod.Delete, request.method)
            assertEquals("/collections/298/remove", request.url.encodedPath)
            assertEquals("p1", request.url.parameters["photo_id"])
            json("""{"photo":$PHOTO,"collection":{"id":298,"title":"API test"}}""")
        }

        assertIs<Response.Success<*>>(CollectionsApi(client).removePhoto(collectionId = 298, photoId = "p1"))
        client.close()
    }

    @Test
    fun createCollectionOmitsUnsetFields() = runBlocking {
        val client = client { request ->
            assertEquals(HttpMethod.Post, request.method)
            val form = assertIs<FormDataContent>(request.body).formData
            assertEquals("Trips", form["title"])
            assertEquals("true", form["private"])
            assertNull(form["description"])
            json("""{"id":1,"title":"Trips","private":true}""")
        }

        val result = CollectionsApi(client).createCollection(CollectionRequest(title = "Trips", isPrivate = true))

        assertIs<Response.Success<*>>(result)
        client.close()
    }

    @Test
    fun deleteCollectionAcceptsEmptyBody() = runBlocking {
        val client = client { request ->
            assertEquals(HttpMethod.Delete, request.method)
            assertEquals("/collections/5", request.url.encodedPath)
            respond(content = "", status = HttpStatusCode.NoContent)
        }

        assertEquals(Response.Success(Unit), CollectionsApi(client).deleteCollection(5))
        client.close()
    }

    @Test
    fun topicsListPassesIdsAndOrder() = runBlocking {
        val client = client { request ->
            assertEquals("/topics", request.url.encodedPath)
            assertEquals("wallpapers,nature", request.url.parameters["ids"])
            assertEquals("featured", request.url.parameters["order_by"])
            json("""[{"id":"t1","slug":"wallpapers","title":"Wallpapers","owners":[],"preview_photos":[$PHOTO]}]""")
        }

        val topic = assertIs<Response.Success<List<TopicResponse>>>(
            TopicsApi(client).getTopics(
                page = 1,
                perPage = 10,
                ids = listOf("wallpapers", "nature"),
                order = TopicsOrder.Featured
            )
        ).data.single()

        assertEquals("wallpapers", topic.slug)
        assertEquals(1, topic.previewPhotos.size)
        client.close()
    }

    @Test
    fun topicKeepsUndocumentedFieldsAsRawJson() = runBlocking {
        val client = client {
            json(
                """{"id":"t1","slug":"s","title":"T","current_user_contributions":[],""" +
                    """"total_current_user_submissions":{"a":1}}"""
            )
        }

        val topic = assertIs<Response.Success<TopicResponse>>(TopicsApi(client).getTopic("t1")).data

        assertEquals(1, topic.totalCurrentUserSubmissions?.size)
        client.close()
    }

    @Test
    fun statsTotalDecodesCounters() = runBlocking {
        val client = client { request ->
            assertEquals("/stats/total", request.url.encodedPath)
            json("""{"photos":10000,"downloads":2000,"views":5000,"downloads_per_second":10,"requests":8000}""")
        }

        val stats = assertIs<Response.Success<TotalStatsResponse>>(StatsApi(client).getTotalStats()).data

        assertEquals(10000L, stats.photos)
        assertEquals(10.0, stats.downloadsPerSecond)
        client.close()
    }
}

private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
    HttpClientFactory(
        networkConfig = StaticNetworkConfig(baseUrl = "https://api.example.com/"),
        headersCollector = HeadersCollectorRepositoryImpl(),
        requestStatsCollector = RequestStatsCollectorImpl(),
        sessionExpiryCollector = SessionExpiryCollectorImpl()
    ).create(engine = MockEngine(handler))

private fun MockRequestHandleScope.json(content: String): HttpResponseData =
    respond(content = content, headers = jsonHeaders())

private fun jsonHeaders() = headersOf(
    HttpHeaders.ContentType,
    ContentType.Application.Json.toString()
)

private const val PHOTO = """{
    "id": "LBI7cgq3pbM",
    "created_at": "2016-05-03T11:00:28-04:00",
    "width": 5245,
    "height": 3497,
    "color": "#60544D",
    "blur_hash": "LoC%a7IoIVxZ_NM|M{s:%hRjWAo0",
    "description": "A man drinking a coffee.",
    "user": {"id": "pXhwzz1JtQU", "username": "poorkane", "name": "Gilbert Kane"},
    "current_user_collections": [{"id": 206, "title": "Makers", "cover_photo": null, "user": null}],
    "urls": {"raw": "https://images.unsplash.com/raw", "regular": "https://images.unsplash.com/regular"},
    "links": {"download_location": "https://api.unsplash.com/photos/LBI7cgq3pbM/download"}
}"""