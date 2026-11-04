package org.kimoteru.wallify.core.common.extensions

private const val MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000L

val Long.millisToSeconds: Long get() = this / 1000
val Long.secondsToMillis: Long get() = this * 1000
val Long.hoursToSeconds: Long get() = this * 60 * 60

val Long.millisToEpochDay: Long get() = this / MILLISECONDS_PER_DAY

/** Formats milliseconds as `mm:ss`, or `hh:mm:ss` when the duration is an hour or longer. */
fun Long.toTimeString(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
    } else {
        "${minutes.twoDigits()}:${seconds.twoDigits()}"
    }
}

/** Short counter: 1_234 is `1.2k`, 3_400_000 is `3.4M`; numbers below a thousand stay as they are. */
fun Long.toCompactString(): String = when {
    this >= BILLION -> toCompactString(unit = BILLION, suffix = "B")
    this >= MILLION -> toCompactString(unit = MILLION, suffix = "M")
    this >= THOUSAND -> toCompactString(unit = THOUSAND, suffix = "k")
    else -> toString()
}

private const val THOUSAND = 1_000L
private const val MILLION = 1_000_000L
private const val BILLION = 1_000_000_000L

private fun Long.toCompactString(unit: Long, suffix: String): String {
    val tenths = this * 10 / unit
    val fraction = tenths % 10
    return if (fraction == 0L) "${tenths / 10}$suffix" else "${tenths / 10}.$fraction$suffix"
}

private fun Long.twoDigits(): String = toString().padStart(2, '0')