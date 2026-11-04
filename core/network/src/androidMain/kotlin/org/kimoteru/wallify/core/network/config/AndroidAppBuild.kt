package org.kimoteru.wallify.core.network.config

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.core.annotation.Singleton

/** A debug build is the one the system marks as debuggable, so no `BuildConfig` of the app is needed. */
@Singleton
internal class AndroidAppBuild(
    context: Context
) : AppBuild {
    override val isDebug: Boolean = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}