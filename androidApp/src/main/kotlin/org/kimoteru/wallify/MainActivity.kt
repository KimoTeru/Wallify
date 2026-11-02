package org.kimoteru.wallify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.feature.root.presentation.components.DefaultRootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphUi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootComponent = DefaultRootGraphComponent(
            componentContext = defaultComponentContext(),
            finishApp = ::finishAffinity
        )

        setContent {
            AppTheme {
                RootGraphUi(component = rootComponent)
            }
        }
    }
}