package org.kimoteru.wallify.core.presentation

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/**
 * Bindings of the shared screen blocks (bottom sheet, error dialog, notifications, permissions).
 * Logic and managers are annotated where they are declared; this class only scans the module package.
 */
@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.core.presentation")
class PresentationModule