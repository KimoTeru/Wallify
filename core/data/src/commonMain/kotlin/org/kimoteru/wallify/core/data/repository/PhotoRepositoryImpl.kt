package org.kimoteru.wallify.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.kimoteru.wallify.core.common.paging.createPager
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.data.remote.PhotoRemoteDataSource
import org.kimoteru.wallify.core.domain.repository.PhotoRepository
import org.kimoteru.wallify.core.model.photo.Photo
import org.kimoteru.wallify.core.model.photo.RandomPhotoQuery
import org.kimoteru.wallify.core.model.photo.UpdatePhotoParams
import org.kimoteru.wallify.core.model.stats.Statistics
import org.koin.core.annotation.Singleton

@Singleton
class PhotoRepositoryImpl(
    private val photoRemoteDataSource: PhotoRemoteDataSource
) : PhotoRepository {

    override fun getPhotosFlow(): Flow<PagingData<Photo>> {
        return createPager { page, loadSize ->
            photoRemoteDataSource.getPhotos(page = page, size = loadSize)
        }
    }

    override suspend fun getPhoto(id: String): Response<Photo> {
        return photoRemoteDataSource.getPhoto(id = id)
    }

    override suspend fun getRandomPhoto(query: RandomPhotoQuery): Response<Photo> {
        return photoRemoteDataSource.getRandomPhoto(query = query)
    }

    override suspend fun getRandomPhotos(count: Int, query: RandomPhotoQuery): Response<List<Photo>> {
        return photoRemoteDataSource.getRandomPhotos(count = count, query = query)
    }

    override suspend fun getPhotoStatistics(id: String, quantity: Int?): Response<Statistics> {
        return photoRemoteDataSource.getPhotoStatistics(id = id, quantity = quantity)
    }

    override suspend fun trackDownload(photoId: String): Response<String> {
        return photoRemoteDataSource.trackDownload(id = photoId)
    }

    override suspend fun updatePhoto(id: String, params: UpdatePhotoParams): Response<Photo> {
        return photoRemoteDataSource.updatePhoto(id = id, params = params)
    }
}