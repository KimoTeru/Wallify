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

fun String.titleCaseFirstChar() = replaceFirstChar(Char::titlecase)

fun String.toNullIf(converter: (String) -> Boolean): String? {
    return if (converter(this)) null else this
}

fun String.nullIfBlank(): String? = ifBlank { null }