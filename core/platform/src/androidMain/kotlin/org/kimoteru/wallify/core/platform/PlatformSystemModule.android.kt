package org.kimoteru.wallify.core.platform

import org.koin.core.annotation.Module

/** Android needs no system object here: the managers get `Context` from the graph. */
@Module
actual class PlatformSystemModule