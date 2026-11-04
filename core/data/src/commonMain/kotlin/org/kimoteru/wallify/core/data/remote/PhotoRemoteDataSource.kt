package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.UpdatePhotoParams
import org.kimoteru.wallify.core.model.stats.Statistics

interface PhotoRemoteDataSource {

    suspend fun getPhotos(page: Int, size: Int): Response<List<Photo>>

    suspend fun getPhoto(id: String): Response<Photo>

    suspend fun getRandomPhoto(query: RandomPhotoQuery): Response<Photo>

    suspend fun getRandomPhotos(count: Int, query: RandomPhotoQuery): Response<List<Photo>>

    suspend fun getPhotoStatistics(id: String, quantity: Int?): Response<Statistics>

    suspend fun trackDownload(id: String): Response<String>

    suspend fun updatePhoto(id: String, params: UpdatePhotoParams): Response<Photo>
}