package org.kimoteru.wallify.core.platform.managers.auth

import kotlinx.coroutines.CompletableDeferred
import org.koin.core.annotation.Singleton

/**
 * Hands the address of the sign-in redirect from the activity (which the system opens through the app link)
 * to [AndroidAuthBrowser], which waits for it. An address that nobody waits for is dropped.
 */
@Singleton
class AuthRedirectBus {

    @Volatile
    internal var pending: CompletableDeferred<String>? = null

    fun publish(url: String) {
        pending?.complete(url)
    }
}