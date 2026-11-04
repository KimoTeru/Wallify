package org.kimoteru.wallify.core.network.config

import org.koin.core.annotation.Singleton

/**
 * Desktop has no build types, so the run tasks of `desktopApp` pass `-Dwallify.debug=true` (`build-logic`);
 * a packaged application starts without it and is a release build.
 */
@Singleton
internal class JvmAppBuild : AppBuild {
    override val isDebug: Boolean = System.getProperty(DEBUG_PROPERTY).toBoolean()

    private companion object {
        const val DEBUG_PROPERTY = "wallify.debug"
    }
}