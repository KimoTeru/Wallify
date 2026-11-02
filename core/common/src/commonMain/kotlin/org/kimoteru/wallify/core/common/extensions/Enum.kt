package org.kimoteru.wallify.core.common.extensions

fun Enum<*>.lowercaseName(): String = name.lowercase()

fun Enum<*>.lowercaseNameWithSpaces(): String = lowercaseName().replace(String.Underscore, String.Space)