package org.kimoteru.wallify.core.platform.managers.auth

import com.sun.net.httpserver.HttpServer
import java.awt.Desktop
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.kimoteru.wallify.core.common.dispatcher.AppDispatchers
import org.koin.core.annotation.Singleton

/**
 * Opens the default browser and catches the redirect with a short-lived HTTP server that listens only on the
 * loopback interface. The server lives exactly as long as the sign-in waits, and no outside machine can reach it.
 */
@Singleton
class JvmAuthBrowser(
    private val dispatchers: AppDispatchers
) : AuthBrowser {

    override val redirectUri: String = "http://$LOOPBACK_HOST:$PORT$CALLBACK_PATH"

    override suspend fun open(authorizationUrl: String): String? = withContext(dispatchers.io) {
        check(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            "The system cannot open a browser"
        }

        val redirect = CompletableDeferred<String>()
        val server = HttpServer.create(InetSocketAddress(InetAddress.getByName(LOOPBACK_HOST), PORT), 0)
        server.createContext(CALLBACK_PATH) { exchange ->
            val requestedUrl = "http://$LOOPBACK_HOST:$PORT${exchange.requestURI}"
            val body = DONE_PAGE.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
            redirect.complete(requestedUrl)
        }
        server.start()

        try {
            Desktop.getDesktop().browse(URI(authorizationUrl))
            withTimeoutOrNull(WAIT_TIMEOUT) { redirect.await() }
        } finally {
            server.stop(0)
        }
    }

    private companion object {
        const val LOOPBACK_HOST = "127.0.0.1"
        const val PORT = 49721
        const val CALLBACK_PATH = "/oauth/callback"
        val WAIT_TIMEOUT = 5.minutes

        // A page of the browser, not of the app, so it cannot use the app resources.
        const val DONE_PAGE = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Wallify</title></head>" +
            "<body style=\"font-family:sans-serif;text-align:center;margin-top:20vh\">" +
            "<h2>You can close this tab and return to Wallify.</h2></body></html>"
    }
}