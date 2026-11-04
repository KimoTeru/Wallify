package org.kimoteru.wallify.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.UpdatePhotoParams
import org.kimoteru.wallify.core.model.stats.Statistics

interface PhotoRepository {

    /** Editorial feed. */
    fun getPhotosFlow(): Flow<PagingData<Photo>>

    suspend fun getPhoto(id: String): Response<Photo>

    suspend fun getRandomPhoto(query: RandomPhotoQuery = RandomPhotoQuery()): Response<Photo>

    /** [count] is 1..30. */
    suspend fun getRandomPhotos(count: Int, query: RandomPhotoQuery = RandomPhotoQuery()): Response<List<Photo>>

    /** [quantity] is the number of days, 1..30; `null` means the backend default. */
    suspend fun getPhotoStatistics(id: String, quantity: Int? = null): Response<Statistics>

    /**
     * Reports that the app downloads the photo; call it every time before the file is fetched.
     * Returns the URL of the file.
     */
    suspend fun trackDownload(photoId: String): Response<String>

    /** Needs the user token with the photo write scope. */
    suspend fun updatePhoto(id: String, params: UpdatePhotoParams): Response<Photo>
}