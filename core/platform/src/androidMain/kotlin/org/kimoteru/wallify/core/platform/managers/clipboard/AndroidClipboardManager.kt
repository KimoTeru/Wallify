package org.kimoteru.wallify.core.platform.managers.clipboard

import android.content.ClipData
import android.content.Context
import android.os.Build
import org.koin.core.annotation.Singleton
import android.content.ClipboardManager as SystemClipboardManager

@Singleton
class AndroidClipboardManager(
    private val context: Context
) : ClipboardManager {
    override fun copyText(text: String, onCopied: () -> Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as SystemClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(null, text))

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            onCopied()
        }
    }
}