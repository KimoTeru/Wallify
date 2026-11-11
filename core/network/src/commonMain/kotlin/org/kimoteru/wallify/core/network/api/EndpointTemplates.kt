package org.kimoteru.wallify.core.network.api

import org.kimoteru.wallify.core.common.extensions.Slash
import org.kimoteru.wallify.core.common.extensions.Space
import org.kimoteru.wallify.core.common.extensions.Star

/**
 * Path templates of every endpoint the API classes call, so request statistics group `photos/abc` and `photos/xyz`
 * under one `GET /photos/{id}`. A new path in an API class needs its template here, otherwise it is counted as
 * its first segment followed by a star (`GET /unknown/` and `*`): a slash with a star would open a nested comment.
 */
internal object EndpointTemplates {
    private val templates: List<List<String>> = listOf(
        "photos",
        "photos/random",
        "photos/{id}",
        "photos/{id}/statistics",
        "photos/{id}/download",
        "collections",
        "collections/{id}",
        "collections/{id}/photos",
        "collections/{id}/related",
        "collections/{id}/add",
        "collections/{id}/remove",
        "search/photos",
        "search/collections",
        "search/users",
        "stats/total",
        "stats/month",
        "topics",
        "topics/{id_or_slug}",
        "topics/{id_or_slug}/photos",
        "me",
        "users/{username}",
        "users/{username}/photos",
        "users/{username}/collections",
        "users/{username}/statistics",
        "oauth/token"
    ).map { template -> template.split(String.Slash) }

    /**
     * Method and template of [encodedPath], e.g. `GET /users/{username}/photos`. A fixed segment wins over a
     * placeholder, so `photos/random` is not taken for `photos/{id}`.
     */
    fun of(method: String, encodedPath: String): String {
        val segments = encodedPath.split(String.Slash).filter { it.isNotEmpty() }
        val template = templates
            .filter { template -> template.matches(segments) }
            .maxByOrNull { template -> template.count { segment -> !segment.isPlaceholder() } }
            ?: segments.unknownTemplate()
        return method + String.Space + String.Slash + template.joinToString(String.Slash)
    }

    private fun List<String>.matches(segments: List<String>): Boolean =
        size == segments.size && zip(segments).all { (template, segment) ->
            template.isPlaceholder() || template == segment
        }

    private fun String.isPlaceholder(): Boolean = startsWith(PLACEHOLDER_START)

    private fun List<String>.unknownTemplate(): List<String> = when {
        isEmpty() -> emptyList()
        size == 1 -> this
        else -> listOf(first(), String.Star)
    }

    private const val PLACEHOLDER_START = "{"
}