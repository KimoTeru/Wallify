package org.kimoteru.wallify.core.data.download

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import org.kimoteru.wallify.core.cache.CacheDirectoryProvider
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.kimoteru.wallify.core.common.extensions.Empty
import org.kimoteru.wallify.core.common.extensions.formatFromUrl
import org.kimoteru.wallify.core.common.extensions.nullIfBlank
import org.kimoteru.wallify.core.common.response.Response
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.common.response.responseOf
import org.kimoteru.wallify.core.network.client.DownloadHttpClient
import org.koin.core.annotation.Singleton

/**
 * Downloads files by URL into the app cache and reuses them on the next call.
 *
 * Concurrent calls for the same file wait for the first download instead of starting another one.
 * The file is written to a temporary path and moved into place, so a failed download never leaves a broken file.
 * It uses the plain download client: no base URL and no API or session headers are sent.
 */
@Singleton
class FileDownloader(
    private val client: DownloadHttpClient,
    private val directoryProvider: CacheDirectoryProvider,
    private val dispatchers: AppDispatchers,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    private val locksGuard = Mutex()
    private val locks = mutableMapOf<String, Mutex>()

    /** Returns the cached file for [url] or downloads it. [name] is the file name without extension. */
    suspend fun getOrDownload(url: String?, name: String): Response<Path> = withContext(dispatchers.io) {
        val safeUrl = url?.nullIfBlank()
            ?: return@withContext Response.Error(ResponseError("Empty URL"))

        val target = directory() / "${name.toSafeFileName()}.${safeUrl.formatFromUrl()}"

        lockFor(target.name).withLock {
            if (isUsable(target)) {
                Response.Success(target)
            } else {
                download(safeUrl, target)
            }
        }
    }

    suspend fun delete(name: String, url: String): Response<Unit> = withContext(dispatchers.io) {
        responseOf {
            val target = directory() / "${name.toSafeFileName()}.${url.formatFromUrl()}"
            if (fileSystem.exists(target)) fileSystem.delete(target)
        }
    }

    /** Deletes every downloaded file. */
    suspend fun clear(): Response<Unit> = withContext(dispatchers.io) {
        locksGuard.withLock {
            responseOf {
                val folder = directory()
                if (fileSystem.exists(folder)) fileSystem.deleteRecursively(folder)
            }
        }
    }

    private suspend fun download(url: String, target: Path): Response<Path> {
        val response = try {
            client.client.get(url)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            return Response.Error(ResponseError(exception.message ?: "Download error"))
        }

        if (!response.status.isSuccess()) {
            return Response.Error(ResponseError(response.status.description, response.status.value))
        }

        return responseOf {
            val bytes = response.body<ByteArray>()
            check(bytes.isNotEmpty()) { "Downloaded file is empty" }

            val folder = directory()
            fileSystem.createDirectories(folder)
            val temp = folder / "${target.name}.tmp"
            fileSystem.write(temp) { write(bytes) }
            fileSystem.atomicMove(temp, target)
            target
        }
    }

    private fun isUsable(path: Path): Boolean =
        fileSystem.exists(path) && (fileSystem.metadataOrNull(path)?.size ?: 0L) > 0L

    private fun directory(): Path = directoryProvider.directory() / DOWNLOADS_DIRECTORY

    private suspend fun lockFor(key: String): Mutex = locksGuard.withLock {
        locks.getOrPut(key) { Mutex() }
    }

    private fun String.toSafeFileName(): String =
        map { char -> if (char.isLetterOrDigit() || char == '-' || char == '_') char else '_' }
            .joinToString(String.Empty)
            .ifEmpty { "file" }

    private companion object {
        const val DOWNLOADS_DIRECTORY = "downloads"
    }
}