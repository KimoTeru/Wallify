package org.kimoteru.wallify.core.data.remote

import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.map
import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.UpdatePhotoParams
import org.kimoteru.wallify.core.model.stats.Statistics
import org.kimoteru.wallify.core.network.api.PhotosApi
import org.kimoteru.wallify.core.network.model.request.photo.toRequest
import org.kimoteru.wallify.core.network.model.response.photo.toDomain
import org.kimoteru.wallify.core.network.model.response.photo.toFileUrl
import org.kimoteru.wallify.core.network.model.response.stats.toDomain
import org.koin.core.annotation.Singleton

@Singleton
class PhotoRemoteDataSourceImpl(
    private val photosApi: PhotosApi
) : PhotoRemoteDataSource {

    override suspend fun getPhotos(page: Int, size: Int): Response<List<Photo>> {
        return photosApi.getPhotos(page = page, perPage = size)
            .map { photos -> photos.map { photo -> photo.toDomain() } }
    }

    override suspend fun getPhoto(id: String): Response<Photo> {
        return photosApi.getPhoto(id = id)
            .map { it.toDomain() }
    }

    override suspend fun getRandomPhoto(query: RandomPhotoQuery): Response<Photo> {
        return photosApi.getRandomPhoto(query = query)
            .map { it.toDomain() }
    }

    override suspend fun getRandomPhotos(count: Int, query: RandomPhotoQuery): Response<List<Photo>> {
        return photosApi.getRandomPhotos(count = count, query = query)
            .map { photos -> photos.map { photo -> photo.toDomain() } }
    }

    override suspend fun getPhotoStatistics(id: String, quantity: Int?): Response<Statistics> {
        return photosApi.getPhotoStatistics(id = id, quantity = quantity)
            .map { it.toDomain() }
    }

    override suspend fun trackDownload(id: String, quality: ImageQuality): Response<String> {
        return photosApi.trackDownload(id = id)
            .map { it.toFileUrl(photoId = id, quality = quality) }
    }

    override suspend fun updatePhoto(id: String, params: UpdatePhotoParams): Response<Photo> {
        return photosApi.updatePhoto(id = id, request = params.toRequest())
            .map { it.toDomain() }
    }
}