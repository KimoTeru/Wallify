package org.kimoteru.wallify.core.common.extensions

val String.Companion.Empty: String
    get() = ""

val String.Companion.Question: String
    get() = "?"

val String.Companion.Dot: String
    get() = "."

val String.Companion.Space: String
    get() = " "

val String.Companion.SemicolonSpace: String
    get() = "; "

val String.Companion.Comma: String
    get() = ","

val String.Companion.CommaSpace: String
    get() = ", "

val String.Companion.Underscore: String
    get() = "_"

val String.Companion.NewLine: String
    get() = "\n"

val String.Companion.X: String
    get() = "x"

val String.Companion.Slash: String
    get() = "/"

val String.Companion.Dash: String
    get() = "-"

val String.Companion.Dollar: String
    get() = "$"

val String.Companion.Percent: String
    get() = "%"

val String.Companion.Bullet: String
    get() = "•"

val String.Companion.Star: String
    get() = "*"

val String.Companion.StartComma: String
    get() = "("

val String.Companion.EndComma: String
    get() = ")"

val String.Companion.Nbsp: String
    get() = "\u00A0"

val String.Companion.Unknown: String
    get() = "unknown"

val String.Companion.Hash: String
    get() = "#"

val String.Companion.At: String
    get() = "@"

fun String.titleCaseFirstChar() = replaceFirstChar(Char::titlecase)

fun String.toNullIf(converter: (String) -> Boolean): String? {
    return if (converter(this)) null else this
}

fun String.nullIfBlank(): String? = ifBlank { null }

private const val MAX_INITIALS = 2
private const val HEX_RADIX = 16
private const val HEX_RGB_LENGTH = 6
private const val OPAQUE_ALPHA = 0xFF000000

/** Up to two first letters of the words of a name, upper case: "Jane van Doe" -> "JV". Empty for a blank name. */
fun String.initials(): String =
    split(String.Space)
        .mapNotNull { word -> word.firstOrNull()?.uppercaseChar() }
        .take(MAX_INITIALS)
        .joinToString(String.Empty)

/** `#RRGGBB` (the `#` is optional) to an opaque ARGB value (`0xFFRRGGBB`), or `null` for anything else. */
fun String.toArgbOrNull(): Long? =
    removePrefix(String.Hash)
        .takeIf { it.length == HEX_RGB_LENGTH }
        ?.toLongOrNull(HEX_RADIX)
        ?.let { rgb -> OPAQUE_ALPHA or rgb }