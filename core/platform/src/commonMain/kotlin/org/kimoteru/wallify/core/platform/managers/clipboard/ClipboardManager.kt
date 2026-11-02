package org.kimoteru.wallify.core.platform.managers.clipboard

interface ClipboardManager {
    /**
     * Copies [text] to the system clipboard.
     * [onCopied] is invoked only when the app should show its own confirmation:
     * Android 13+ shows a system toast, so it is skipped there.
     */
    fun copyText(text: String, onCopied: () -> Unit = {})
}