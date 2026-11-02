package org.kimoteru.wallify.core.common.extensions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/**
 * Parses a JSON object into a flat map. Primitives become Boolean, Long, Double or String,
 * nested objects and arrays are kept as raw JSON strings. Invalid input gives an empty map.
 */
fun String.jsonToMap(json: Json = Json): Map<String, Any?> = try {
    json.parseToJsonElement(this).jsonObject.mapValues { (_, value) ->
        when (value) {
            is JsonNull -> null
            is JsonPrimitive -> when {
                value.isString -> value.content
                else -> value.booleanOrNull ?: value.longOrNull ?: value.doubleOrNull ?: value.content
            }
            else -> value.toString()
        }
    }
} catch (_: Exception) {
    emptyMap()
}