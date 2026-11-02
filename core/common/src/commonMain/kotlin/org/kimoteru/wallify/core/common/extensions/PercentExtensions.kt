package org.kimoteru.wallify.core.common.extensions

fun Float.clampToPercent(): Float {
    if (isNaN()) return 0f
    return when {
        this < 0f -> 0f
        this > 1f -> 1f
        else -> this
    }
}

fun Double.clampToPercent(): Double {
    if (isNaN()) return 0.0
    return when {
        this < 0.0 -> 0.0
        this > 1.0 -> 1.0
        else -> this
    }
}

/**
 * Converts a normalized fraction (0..1) to a pixel offset within [total].
 * Offsets can be 0, so no minimum is forced.
 */
fun Float.toPixelOffset(total: Int): Int {
    if (isNaN()) return 0
    return (clampToPercent() * total).toInt()
}

/**
 * Converts a normalized fraction (0..1) to a pixel size within [total].
 * Sizes must be positive, so the result is at least 1 pixel.
 */
fun Float.toPixelSize(total: Int): Int {
    if (isNaN()) return 1
    val px = (clampToPercent() * total).toInt()
    return if (px < 1) 1 else px
}