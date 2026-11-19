package org.kimoteru.wallify.core.platform

import java.awt.Desktop
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
actual class PlatformSystemModule {
    /**
     * Throws where the system has no desktop support (headless, some Linux environments), so the managers take it as
     * `Lazy<Desktop>` and reach it only when they open something, inside their own error handling.
     */
    @Singleton
    fun desktop(): Desktop {
        check(Desktop.isDesktopSupported()) { "The system has no desktop support" }
        return Desktop.getDesktop()
    }
}