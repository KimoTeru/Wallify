package org.kimoteru.wallify.core.common.extensions

private val isoYearPrefix = Regex("""^(\d{4})-\d{2}-\d{2}""")

/** Extracts the year from an ISO-8601 date or date-time string, or null if the text is not one. */
fun String.toYear(): Int? = isoYearPrefix.find(trim())?.groupValues?.get(1)?.toIntOrNull()