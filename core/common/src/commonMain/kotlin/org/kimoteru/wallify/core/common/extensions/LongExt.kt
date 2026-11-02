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

private fun Long.twoDigits(): String = toString().padStart(2, '0')