package org.kimoteru.wallify

import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.feature.root.presentation.components.DefaultRootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphUi
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

/**
 * Entry point of the dependency graph: the compiler plugin finds every `@Module @Configuration` among the
 * dependencies of this module and checks the whole graph at build time.
 */
@KoinApplication
class DesktopApp

fun main() {
    startKoin<DesktopApp>()

    val lifecycle = LifecycleRegistry()
    var exitApp: () -> Unit = {}

    val rootComponent = runOnUiThread {
        DefaultRootGraphComponent(
            componentContext = DefaultComponentContext(lifecycle),
            finishApp = { exitApp() }
        )
    }

    application {
        exitApp = ::exitApplication

        val windowState = rememberWindowState()

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Wallify"
        ) {
            LifecycleController(
                lifecycleRegistry = lifecycle,
                windowState = windowState,
                windowInfo = LocalWindowInfo.current
            )

            AppTheme {
                RootGraphUi(component = rootComponent)
            }
        }
    }
}