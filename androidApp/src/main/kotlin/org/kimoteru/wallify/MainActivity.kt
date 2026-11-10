package org.kimoteru.wallify

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import com.arkivanov.decompose.defaultComponentContext
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.locale.ProvideAppLocale
import org.kimoteru.wallify.core.platform.managers.auth.AuthRedirectBus
import org.kimoteru.wallify.core.presentation.settings.isDarkTheme
import org.kimoteru.wallify.feature.root.presentation.components.DefaultRootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphUi
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val authRedirectBus: AuthRedirectBus by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootComponent = DefaultRootGraphComponent(
            componentContext = defaultComponentContext(),
            finishApp = ::finishAffinity
        )

        setContent {
            val uiState = rootComponent.getUiState()
            val isDarkTheme = uiState.themeMode.isDarkTheme()

            // The system bars follow the scheme chosen in the app, not the one of the device.
            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDarkTheme }
                )
                onDispose {}
            }

            ProvideAppLocale(languageTag = uiState.languageTag) {
                AppTheme(isDarkTheme = isDarkTheme) {
                    RootGraphUi(component = rootComponent)
                }
            }
        }
    }

    /** The browser comes back with the sign-in result through the app link; `singleTask` delivers it here. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            intent.dataString?.let(authRedirectBus::publish)
        }
    }
}