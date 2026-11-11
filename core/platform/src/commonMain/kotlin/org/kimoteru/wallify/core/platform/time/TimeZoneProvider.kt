package org.kimoteru.wallify.core.platform.time

/** The time zone of the device: there is no date library in the project, so local days are counted from it. */
interface TimeZoneProvider {
    /** Offset of the device's local time from UTC at [epochMillis], daylight saving time included. */
    fun offsetMillis(epochMillis: Long): Long
}