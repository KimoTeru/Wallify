package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Resource
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.UpdatePhotoParams
import org.kimoteru.wallify.core.model.stats.Statistics

interface PhotoRepository {

    /** Editorial feed. */
    fun getPhotosFlow(): Flow<PagingData<Photo>>

    /**
     * The photo from the database, refreshed from the backend on every collection (`Resource` says how:
     * [Resource.Loading] while nothing is saved, [Resource.Error] if nothing is saved and the backend failed).
     * Collect it again to retry. A photo known only from a list is emitted first with the summary fields.
     */
    fun getPhotoFlow(id: String): Flow<Resource<Photo>>

    /** Every call asks the backend for a new photo and saves it. */
    suspend fun getRandomPhoto(query: RandomPhotoQuery = RandomPhotoQuery()): Response<Photo>

    /**
     * [count] is 1..30. Every call asks the backend for a new set and saves it; when the backend cannot answer
     * (no connection, the request limit), the last saved set for the same request comes instead of an error.
     */
    suspend fun getRandomPhotos(count: Int, query: RandomPhotoQuery = RandomPhotoQuery()): Response<List<Photo>>

    /** [quantity] is the number of days, 1..30; `null` means the backend default. Behaves like [getPhotoFlow]. */
    fun getPhotoStatisticsFlow(id: String, quantity: Int? = null): Flow<Resource<Statistics>>

    /**
     * Reports that the app downloads the photo; call it every time before the file is fetched.
     * Returns the URL of the file.
     */
    suspend fun trackDownload(photoId: String): Response<String>

    /** Needs the user token with the photo write scope. */
    suspend fun updatePhoto(id: String, params: UpdatePhotoParams): Response<Photo>
}