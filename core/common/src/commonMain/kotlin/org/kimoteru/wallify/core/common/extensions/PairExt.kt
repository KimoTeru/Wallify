package org.kimoteru.wallify.core.common.extensions

fun Pair<Int, Int>.min(): Int = minOf(first, second)
fun Pair<Int, Int>.max(): Int = maxOf(first, second)