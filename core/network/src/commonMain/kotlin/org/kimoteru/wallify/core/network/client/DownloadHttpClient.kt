package org.kimoteru.wallify.core.network.client

import io.ktor.client.HttpClient

/** Marker wrapper for the plain download client, see [HttpClientFactory.createPlain]. */
class DownloadHttpClient(val client: HttpClient)