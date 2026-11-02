package org.kimoteru.wallify.core.platform.managers.clipboard

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import org.koin.core.annotation.Singleton

@Singleton
class JvmClipboardManager : ClipboardManager {
    override fun copyText(text: String, onCopied: () -> Unit) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        onCopied()
    }
}