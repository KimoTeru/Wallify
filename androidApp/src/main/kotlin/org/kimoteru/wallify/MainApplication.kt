package org.kimoteru.wallify

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

/**
 * Entry point of the dependency graph: the compiler plugin finds every `@Module @Configuration` among the
 * dependencies of this module and checks the whole graph at build time.
 */
@KoinApplication
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin<MainApplication> {
            androidContext(this@MainApplication)
        }
    }
}