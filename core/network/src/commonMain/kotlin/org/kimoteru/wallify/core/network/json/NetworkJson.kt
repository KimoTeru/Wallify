package org.kimoteru.wallify.core.network.json

import kotlinx.serialization.json.Json

val networkJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}