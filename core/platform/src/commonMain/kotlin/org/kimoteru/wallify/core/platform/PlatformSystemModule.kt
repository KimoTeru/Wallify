package org.kimoteru.wallify.core.platform

import org.koin.core.annotation.Module

/**
 * System objects of one platform that cannot carry a Koin annotation (types of the JDK or the OS), provided by
 * functions of the `actual` module. [PlatformModule] includes it.
 */
@Module
expect class PlatformSystemModule