package org.kimoteru.wallify.core.platform

import java.util.Locale

actual fun systemLanguageTag(): String = Locale.getDefault().toLanguageTag()