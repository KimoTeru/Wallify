package org.kimoteru.wallify.core.platform.managers.intent

import org.koin.core.annotation.Singleton

@Singleton
class JvmIntentNavigator : IntentNavigator {
    override fun openAppSettings() = Unit
    override fun openNotificationSettings() = Unit
}