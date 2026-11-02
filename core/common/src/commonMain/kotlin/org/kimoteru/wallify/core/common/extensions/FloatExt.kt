package org.kimoteru.wallify.core.common.extensions

val Float.times1_5: Float get() = this * 1.5f
val Float.times2: Float get() = this * 2f
val Float.times3: Float get() = this * 3f
val Float.times4: Float get() = this * 4f

val Float.half: Float get() = this / 2f
val Float.quarter: Float get() = this / 4f
val Float.fifth: Float get() = this / 5f

fun Float.mapRange(
    fromMin: Float,
    fromMax: Float,
    toMin: Float,
    toMax: Float
): Float {
    if (fromMin == fromMax) return toMin
    val clamped = this.coerceIn(fromMin.coerceAtMost(fromMax), fromMin.coerceAtLeast(fromMax))
    return ((clamped - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin)
}