package org.kimoteru.wallify.core.common.log

import co.touchlab.kermit.Logger

private const val DEFAULT_TAG = "Wallify"

// The global Kermit `Logger` writes to Logcat on Android and the console on Desktop. Release builds have no calls of
// `log` at all: R8 and ProGuard remove them (`-assumenosideeffects` in `proguard-rules.pro` of both apps).

/** Writes [message] under the app tag; the message is built only when the log is written. */
fun log(message: () -> String) = log(DEFAULT_TAG, message)

/** Writes [message] under [tag]; the message is built only when the log is written. */
fun log(tag: String, message: () -> String) = Logger.d(tag = tag, message = message)