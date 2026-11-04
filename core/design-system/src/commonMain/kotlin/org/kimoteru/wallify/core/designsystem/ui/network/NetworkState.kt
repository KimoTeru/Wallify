package org.kimoteru.wallify.core.designsystem.ui.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_dialog_description
import org.kimoteru.wallify.core.resources.generated.resources.error_dialog_title
import org.kimoteru.wallify.core.resources.generated.resources.error_no_internet
import org.kimoteru.wallify.core.resources.generated.resources.error_no_internet_description

/** Whether the device is online. The root provides it, so a failed list can say "no internet", not "wrong". */
val LocalIsOnline = staticCompositionLocalOf { true }

/** Title of an error state: the lack of internet when offline, a general failure otherwise. */
@Composable
fun networkErrorTitle(): String =
    stringResource(if (LocalIsOnline.current) Res.string.error_dialog_title else Res.string.error_no_internet)

/** Description of an error state; offline it promises that the content comes back by itself. */
@Composable
fun networkErrorDescription(): String =
    stringResource(
        if (LocalIsOnline.current) Res.string.error_dialog_description else Res.string.error_no_internet_description
    )

/**
 * Calls [onRetry] when the connection returns while [isFailed] is true, so a screen that failed to load offline
 * recovers without the user pressing anything. Only the return of the connection triggers it: a failure online
 * is not repeated in a loop. Writes must not use it (a repeated request may duplicate the first one).
 */
@Composable
fun RetryOnReconnect(
    isFailed: Boolean,
    onRetry: () -> Unit
) {
    val isOnline = LocalIsOnline.current
    val currentIsFailed by rememberUpdatedState(isFailed)
    val currentOnRetry by rememberUpdatedState(onRetry)
    var wasOffline by remember { mutableStateOf(!isOnline) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            wasOffline = true
        } else if (wasOffline) {
            wasOffline = false
            if (currentIsFailed) currentOnRetry()
        }
    }
}