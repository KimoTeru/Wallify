package org.kimoteru.wallify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import java.awt.Dimension
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppSizes
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.app_name
import org.kimoteru.wallify.feature.root.presentation.components.DefaultRootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphUi
import org.kimoteru.wallify.feature.root.presentation.ui.isDarkTheme
import org.kimoteru.wallify.feature.root.presentation.ui.isSettingsLoaded
import org.kimoteru.wallify.feature.root.presentation.ui.requestedWindowType
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

/**
 * Entry point of the dependency graph: the compiler plugin finds every `@Module @Configuration` among the
 * dependencies of this module and checks the whole graph at build time.
 */
@KoinApplication
class DesktopApp

private const val WINDOW_SAVE_DELAY_MILLIS = 500L

/** macOS keeps its system window (with a transparent title bar); elsewhere the app draws the frame itself. */
private val IS_MAC = System.getProperty("os.name").orEmpty().startsWith("Mac")

@OptIn(FlowPreview::class)
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

    val windowStore = WindowStateStore()
    val storedWindow = windowStore.restore(minSize = AppSizes().window.let { DpSize(it.minWidth, it.minHeight) })

    application {
        exitApp = ::exitApplication

        val windowState = rememberWindowState(
            placement = storedWindow.placement,
            position = storedWindow.position,
            size = storedWindow.size
        )

        // Picking a screen layout in the settings gives the window a width of that layout. The first value read from
        // the settings is only the starting point, so the saved window size is kept at the start.
        val isSettingsLoaded = rootComponent.isSettingsLoaded()
        val requestedWindowType = rootComponent.requestedWindowType()
        val windowSizes = AppTheme.size.window
        var appliedWindowType by remember { mutableStateOf<WindowType?>(null) }
        var isBaselineSet by remember { mutableStateOf(false) }

        // The window cannot be made smaller than the screens can lay out; a forced layout needs the room of its
        // navigation.
        val minimumWidth = when (requestedWindowType) {
            WindowType.Medium -> windowSizes.mediumMinWidth
            WindowType.Expanded -> windowSizes.expandedMinWidth
            WindowType.Compact, null -> windowSizes.minWidth
        }

        LaunchedEffect(isSettingsLoaded, requestedWindowType) {
            if (!isSettingsLoaded) return@LaunchedEffect

            if (!isBaselineSet) {
                isBaselineSet = true
                appliedWindowType = requestedWindowType
                return@LaunchedEffect
            }

            if (requestedWindowType == appliedWindowType) return@LaunchedEffect
            appliedWindowType = requestedWindowType

            val width = when (requestedWindowType) {
                WindowType.Compact -> windowSizes.compactWidth
                WindowType.Medium -> windowSizes.mediumWidth
                WindowType.Expanded -> windowSizes.expandedWidth
                null -> return@LaunchedEffect
            }

            if (windowState.placement != WindowPlacement.Floating) {
                windowState.placement = WindowPlacement.Floating
            }

            windowState.size = DpSize(width.coerceAtLeast(windowSizes.minWidth), windowState.size.height)
        }

        // Resizing fires many changes; the geometry is written once the user stops.
        LaunchedEffect(windowState) {
            snapshotFlow { Triple(windowState.size, windowState.position, windowState.placement) }
                .debounce(WINDOW_SAVE_DELAY_MILLIS)
                .collect { windowStore.save(windowState) }
        }

        val closeWindow = {
            windowStore.save(windowState)
            exitApplication()
        }

        val title = stringResource(Res.string.app_name)

        Window(
            onCloseRequest = closeWindow,
            state = windowState,
            title = title,
            undecorated = !IS_MAC
        ) {
            // A window already below the new minimum is enlarged by the platform.
            LaunchedEffect(minimumWidth, windowSizes) {
                window.minimumSize = Dimension(minimumWidth.value.toInt(), windowSizes.minHeight.value.toInt())
            }

            LifecycleController(
                lifecycleRegistry = lifecycle,
                windowState = windowState,
                windowInfo = LocalWindowInfo.current
            )

            val isDarkTheme = rootComponent.isDarkTheme()

            // On macOS the title bar is transparent and the app draws under it (hence the inset), with the window
            // buttons in the tone of the scheme.
            if (IS_MAC) {
                LaunchedEffect(isDarkTheme) {
                    val rootPane = window.rootPane
                    rootPane.putClientProperty(
                        "apple.awt.windowAppearance",
                        if (isDarkTheme) "NSAppearanceNameDarkAqua" else "NSAppearanceNameAqua"
                    )
                    rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                    rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                }
            }

            AppTheme(isDarkTheme = isDarkTheme) {
                val appColor = AppTheme.color

                if (IS_MAC) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(appColor.basic.background)
                            .padding(top = windowSizes.titleBarHeight)
                    ) {
                        RootGraphUi(component = rootComponent)
                    }
                } else {
                    WindowFrame(
                        windowState = windowState,
                        title = title,
                        minSize = DpSize(minimumWidth, windowSizes.minHeight),
                        onClose = closeWindow
                    ) {
                        RootGraphUi(component = rootComponent)
                    }
                }
            }
        }
    }
}