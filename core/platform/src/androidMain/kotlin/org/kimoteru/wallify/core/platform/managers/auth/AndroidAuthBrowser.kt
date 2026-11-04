package org.kimoteru.wallify.core.platform.managers.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Singleton

/**
 * Opens the system browser. The redirect comes back as an app link (`wallify://oauth/callback`) that
 * `MainActivity` receives and passes to [AuthRedirectBus].
 */
@Singleton
class AndroidAuthBrowser(
    private val context: Context,
    private val redirectBus: AuthRedirectBus
) : AuthBrowser {

    override val redirectUri: String = REDIRECT_URI

    override suspend fun open(authorizationUrl: String): String? {
        val redirect = CompletableDeferred<String>()
        redirectBus.pending = redirect

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            return withTimeoutOrNull(WAIT_TIMEOUT) { redirect.await() }
        } finally {
            redirectBus.pending = null
        }
    }

    private companion object {
        const val REDIRECT_URI = "wallify://oauth/callback"
        val WAIT_TIMEOUT = 5.minutes
    }
}