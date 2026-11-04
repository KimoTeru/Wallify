package org.kimoteru.wallify.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.kimoteru.wallify.core.network.model.response.photo.PhotoLinksResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoLocationResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoResponse
import org.kimoteru.wallify.core.network.model.response.photo.PhotoUrlsResponse
import org.kimoteru.wallify.core.network.model.response.photo.TagResponse
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.kimoteru.wallify.core.network.model.response.stats.HistoricalStatResponse
import org.kimoteru.wallify.core.network.model.response.stats.PhotoStatisticsResponse
import org.kimoteru.wallify.core.network.model.response.stats.StatSeriesResponse
import org.kimoteru.wallify.core.network.model.response.stats.StatValueResponse
import org.kimoteru.wallify.core.network.model.response.stats.toDomain
import org.kimoteru.wallify.core.network.model.response.user.ProfileImageResponse
import org.kimoteru.wallify.core.network.model.response.user.UserResponse
import org.kimoteru.wallify.core.network.model.response.user.toDomain

class ResponseMappersTest {

    @Test
    fun photoKeepsOnlyWhatTheAppNeeds() {
        val photo = PhotoResponse(
            id = "p1",
            width = 100,
            height = 50,
            description = " ",
            altDescription = "man drinking coffee",
            location = PhotoLocationResponse(city = "Montreal", country = "Canada"),
            tags = listOf(TagResponse("man"), TagResponse("coffee")),
            urls = PhotoUrlsResponse(raw = "raw", regular = "regular"),
            links = PhotoLinksResponse(html = "page", downloadLocation = "track")
        ).toDomain()

        assertEquals("man drinking coffee", photo.description)
        assertEquals("Montreal, Canada", photo.location)
        assertEquals(listOf("man", "coffee"), photo.tags)
        assertEquals("page?utm_source=Wallify&utm_medium=referral", photo.htmlUrl)
        assertEquals("track", photo.downloadLocation)
        assertNull(photo.downloads)
        assertNull(photo.exif)
        assertEquals(emptyList(), photo.currentUserCollectionIds)
        assertNull(photo.user)
    }

    @Test
    fun referralIsAppendedToLinksThatAlreadyHaveAQuery() {
        val photo = PhotoResponse(
            id = "p2",
            urls = PhotoUrlsResponse(raw = "raw"),
            links = PhotoLinksResponse(html = "page?a=b")
        ).toDomain()

        assertEquals("page?a=b&utm_source=Wallify&utm_medium=referral", photo.htmlUrl)
    }

    @Test
    fun missingUrlSizesFallBackToRaw() {
        val urls = PhotoUrlsResponse(raw = "raw", thumb = "thumb").toDomain()

        assertEquals("raw", urls.full)
        assertEquals("raw", urls.regular)
        assertEquals("thumb", urls.thumb)
    }

    @Test
    fun userNameIsBuiltFromFirstAndLastNameForCurrentUser() {
        val user = UserResponse(id = "u1", username = "jimmy", firstName = "James", lastName = "Example").toDomain()

        assertEquals("James Example", user.name)
    }

    @Test
    fun userFallsBackToUsernameAndMissingAvatarSizes() {
        val response = UserResponse(
            id = "u1",
            username = "jimmy",
            profileImage = ProfileImageResponse(medium = "medium")
        )

        val user = response.toDomain()

        assertEquals("jimmy", user.name)
        assertEquals("medium", user.profileImage?.large)
        assertEquals(0, user.totalPhotos)
    }

    @Test
    fun statisticsFlattenHistoryAndTolerateMissingSeries() {
        val statistics = PhotoStatisticsResponse(
            downloads = StatSeriesResponse(
                total = 49771,
                historical = HistoricalStatResponse(change = 1474, values = listOf(StatValueResponse("2017-02-27", 16)))
            )
        ).toDomain()

        assertEquals(49771L, statistics.downloads.total)
        assertEquals(1474L, statistics.downloads.change)
        assertEquals("2017-02-27", statistics.downloads.values.single().date)
        assertEquals(0L, statistics.views.total)
        assertEquals(emptyList(), statistics.views.values)
    }
}