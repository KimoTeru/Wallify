package org.kimoteru.wallify.core.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers

class DiskCache(
    private val directory: Path,
    private val appDispatchers: AppDispatchers,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    private val mutex = Mutex()

    suspend fun read(key: String): ByteArray? = mutex.withLock {
        withContext(appDispatchers.io) {
            val path = file(key)
            if (!fileSystem.exists(path)) {
                null
            } else {
                fileSystem.read(path) { readByteArray() }
            }
        }
    }

    suspend fun write(key: String, bytes: ByteArray) {
        mutex.withLock {
            withContext(appDispatchers.io) {
                fileSystem.createDirectories(directory)
                fileSystem.write(file(key)) { write(bytes) }
            }
        }
    }

    suspend fun readText(key: String): String? = read(key)?.decodeToString()

    suspend fun writeText(key: String, value: String) {
        write(key, value.encodeToByteArray())
    }

    suspend fun remove(key: String) {
        mutex.withLock {
            withContext(appDispatchers.io) {
                val path = file(key)
                if (fileSystem.exists(path)) {
                    fileSystem.delete(path)
                }
            }
        }
    }

    suspend fun clear() {
        mutex.withLock {
            withContext(appDispatchers.io) {
                if (!fileSystem.exists(directory)) return@withContext
                fileSystem.list(directory).forEach { child ->
                    fileSystem.delete(child)
                }
            }
        }
    }

    private fun file(key: String): Path = directory / fileName(key)

    private fun fileName(key: String): String {
        var hash = 0x811c9dc5.toInt()
        for (byte in key.encodeToByteArray()) {
            hash = hash xor (byte.toInt() and 0xFF)
            hash *= 0x01000193
        }
        val safe = buildString {
            for (char in key.take(MAX_NAME_PREFIX)) {
                append(if (char.isLetterOrDigit() || char == '-' || char == '_') char else '_')
            }
        }.ifEmpty { "entry" }
        return "${safe}_${hash.toUInt().toString(16)}"
    }

    private companion object {
        const val MAX_NAME_PREFIX = 48
    }
}