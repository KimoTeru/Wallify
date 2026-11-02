package org.kimoteru.wallify

import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import javax.swing.SwingUtilities

/**
 * Runs [block] on the UI (AWT event dispatch) thread and returns its result, blocking the caller until it is done.
 * An exception from [block] is rethrown as is, without wrappers.
 */
internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) return block()

    val task = FutureTask(block)
    SwingUtilities.invokeAndWait(task)

    return try {
        task.get()
    } catch (e: ExecutionException) {
        throw e.cause ?: e
    }
}