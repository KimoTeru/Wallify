package org.kimoteru.wallify.core.platform.managers.intent

import java.awt.Desktop
import java.net.URI
import org.koin.core.annotation.Singleton

@Singleton
class JvmIntentNavigator(
    private val desktop: Lazy<Desktop>
) : IntentNavigator {
    override fun openAppSettings() = Unit
    override fun openNotificationSettings() = Unit

    override fun openUrl(url: String) {
        runCatching { desktop.value.browse(URI(url)) }
    }
}