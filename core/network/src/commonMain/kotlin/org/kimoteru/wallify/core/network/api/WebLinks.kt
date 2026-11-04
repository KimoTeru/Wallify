package org.kimoteru.wallify.core.network.api

import org.kimoteru.wallify.core.network.config.WEB_BASE_URL
import org.koin.core.annotation.Singleton

/**
 * Pages of the backend's website for what the API cannot do. The app opens them in the browser; they are not requests
 * and need no session headers.
 */
@Singleton
class WebLinks {

    /** Page where a signed-in user uploads photos: the API has no upload endpoint. */
    val uploadPageUrl: String = WEB_BASE_URL + UPLOAD_PATH

    private companion object {
        const val UPLOAD_PATH = "submit"
    }
}