package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.kimoteru.wallify.core.designsystem.ui.adaptive.LocalWindowTypeOverride
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurHost
import org.kimoteru.wallify.core.designsystem.ui.network.LocalIsOnline
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridColumns
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.feature.auth.presentation.ui.AuthUi
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoUi
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileUi
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphUi
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent.Child
import org.kimoteru.wallify.feature.splash.presentation.ui.SplashUi

/**
 * The root screens with the transition between them. The user's choices that every screen reads (photo layout,
 * columns, screen layout, connection) are provided once for the whole stack.
 *
 * Every screen is its own [BlurHost] (the Haze way: the state lives where the source and its bars are). During a
 * transition the leaving and the arriving screens are both on the stage, and with one blur state for the whole stack
 * the bars of the leaving screen would blur the arriving one. Nothing above the stack blurs it, so the stack is no
 * blur source itself.
 */
@Composable
internal fun RootGraphStack(
    modifier: Modifier = Modifier,
    component: RootGraphComponent,
    uiState: RootGraphContract.UiState
) {
    CompositionLocalProvider(
        LocalPhotoGridMode provides uiState.photoLayout.gridMode,
        LocalPhotoGridColumns provides uiState.photoColumns,
        LocalWindowTypeOverride provides uiState.screenLayout.windowType,
        LocalIsOnline provides uiState.isOnline
    ) {
        Children(
            stack = component.stack,
            modifier = modifier,
            animation = stackAnimation { fade() + scale() }
        ) { child ->
            BlurHost(modifier = Modifier.fillMaxSize()) {
                RootGraphScreen(instance = child.instance)
            }
        }
    }
}

@Composable
private fun RootGraphScreen(instance: Child) {
    when (instance) {
        is Child.Splash -> SplashUi(component = instance.component)
        is Child.Main -> MainGraphUi(component = instance.component)
        is Child.Photo -> PhotoUi(component = instance.component)
        is Child.Profile -> ProfileUi(component = instance.component)
        is Child.Auth -> AuthUi(component = instance.component)
    }
}