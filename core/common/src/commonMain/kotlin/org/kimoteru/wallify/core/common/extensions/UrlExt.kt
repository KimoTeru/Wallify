package org.kimoteru.wallify.core.common.extensions

/** File name without extension taken from the last path segment of a URL. */
fun String.fileNameFromUrl(defaultName: String = "file"): String {
    val clean = lastPathSegment()
    if (clean.isBlank()) return defaultName

    val lastDot = clean.lastIndexOf('.')
    val name = if (lastDot > 0) clean.take(lastDot) else clean
    return name.ifBlank { defaultName }
}

/** Lowercase file extension of a URL's last path segment, or `bin` when there is none. */
fun String.formatFromUrl(): String {
    val ext = lastPathSegment().substringAfterLast('.', missingDelimiterValue = String.Empty).trim().trimStart('.')
    return ext.lowercase().ifBlank { "bin" }
}

private fun String.lastPathSegment(): String {
    val withoutQuery = substringBefore('#').substringBefore('?')
    val path = if ("://" in withoutQuery) {
        withoutQuery.substringAfter("://").substringAfter('/', missingDelimiterValue = String.Empty)
    } else {
        withoutQuery
    }
    return path.trimEnd('/').substringAfterLast('/').percentDecode().trim()
}

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

/** Decodes `%XX` sequences as UTF-8. Malformed sequences are kept as they are. */
fun String.percentDecode(): String {
    if ('%' !in this) return this

    val bytes = ArrayList<Byte>(length)
    var index = 0
    while (index < length) {
        val char = this[index]
        val hex = if (char == '%' && index + 2 <= lastIndex) {
            val digits = substring(index + 1, index + 3)
            if (digits.all { it.isHexDigit() }) digits.toInt(16) else null
        } else {
            null
        }
        if (hex != null) {
            bytes.add(hex.toByte())
            index += 3
        } else {
            char.toString().encodeToByteArray().forEach(bytes::add)
            index++
        }
    }
    return bytes.toByteArray().decodeToString()
}