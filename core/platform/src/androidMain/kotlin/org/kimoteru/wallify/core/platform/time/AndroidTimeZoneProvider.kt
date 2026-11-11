package org.kimoteru.wallify.core.platform.time

import java.util.TimeZone
import org.koin.core.annotation.Singleton

/** Reads the zone on every call: the user may change it while the app runs. */
@Singleton
class AndroidTimeZoneProvider : TimeZoneProvider {
    override fun offsetMillis(epochMillis: Long): Long = TimeZone.getDefault().getOffset(epochMillis).toLong()
}