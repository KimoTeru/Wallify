package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurHost
import org.kimoteru.wallify.core.model.settings.SupportedLanguages
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerUi
import org.kimoteru.wallify.core.presentation.settings.PhotoLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ScreenLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ThemeModeUi
import org.kimoteru.wallify.feature.auth.presentation.ui.AuthUi
import org.kimoteru.wallify.feature.main.presentation.ui.MainGraphUi
import org.kimoteru.wallify.feature.photo.presentation.ui.PhotoUi
import org.kimoteru.wallify.feature.profile.presentation.ui.ProfileUi
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent.Child
import org.kimoteru.wallify.feature.root.presentation.ui.RootGraphContract.UiEvent
import org.kimoteru.wallify.feature.splash.presentation.ui.SplashUi

object RootGraphContract {
    @Immutable
    data class UiState(
        val notifierManagerState: NotifierManagerContract.UiState = NotifierManagerContract.UiState(),
        val themeMode: ThemeModeUi = ThemeModeUi.System,
        val photoLayout: PhotoLayoutUi = PhotoLayoutUi.Masonry,
        val photoColumns: Int? = null,
        val screenLayout: ScreenLayoutUi = ScreenLayoutUi.Auto,

        /** Language of the app, one of [SupportedLanguages]. */
        val languageTag: String = SupportedLanguages.first().tag,

        /** `false` until the saved settings are read; the values above are defaults until then. */
        val isSettingsLoaded: Boolean = false
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data class NotifierManagerEvent(val event: NotifierManagerContract.UiEvent) : UiEvent
    }
}

/**
 * The root screens with the transition between them. Every screen is its own [BlurHost]: the screen is the blur source
 * and its sheets and dialogs are drawn next to it (the Haze way: the state lives where the source and its effects
 * are). During a transition the leaving and the arriving screens are both on the stage, and with one blur state for
 * the whole stack the sheet of the leaving screen would blur the arriving one. Nothing above the stack blurs it, so
 * the stack is no blur source itself.
 */
@Composable
fun RootGraphUi(
    modifier: Modifier = Modifier,
    component: RootGraphComponent
) {
    val uiState = component.getUiState()
    val obtainUiEvent = component::obtainUiEvent

    val appColor = AppTheme.color

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appColor.basic.background)
    ) {
        Children(
            stack = component.stack,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation { fade() + scale() }
        ) { child ->
            BlurHost(modifier = Modifier.fillMaxSize()) {
                when (val instance = child.instance) {
                    is Child.Splash -> SplashUi(component = instance.component)
                    is Child.Main -> MainGraphUi(component = instance.component)
                    is Child.Photo -> PhotoUi(component = instance.component)
                    is Child.Profile -> ProfileUi(component = instance.component)
                    is Child.Auth -> AuthUi(component = instance.component)
                }
            }
        }
    }

    NotifierManagerUi(
        uiState = uiState.notifierManagerState,
        obtainUiEvent = { obtainUiEvent(UiEvent.NotifierManagerEvent(it)) }
    )
}