package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.adaptive.LocalWindowTypeOverride
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurHost
import org.kimoteru.wallify.core.designsystem.ui.blur.blurSource
import org.kimoteru.wallify.core.designsystem.ui.image.ClearImageCacheEffect
import org.kimoteru.wallify.core.designsystem.ui.network.LocalIsOnline
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridColumns
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.model.settings.PhotoLayout
import org.kimoteru.wallify.core.model.settings.ScreenLayout
import org.kimoteru.wallify.core.model.settings.ThemeMode
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerContract
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerUi
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerUi
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.error_no_internet
import org.kimoteru.wallify.feature.auth.ui.AuthUi
import org.kimoteru.wallify.feature.photo.ui.PhotoUi
import org.kimoteru.wallify.feature.profile.ui.ProfileUi
import org.kimoteru.wallify.feature.root.main.MainGraphUi
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent.Child
import org.kimoteru.wallify.feature.splash.ui.SplashUi

object RootGraphContract {
    @Immutable
    data class UiState(
        val apiErrorManagerState: ApiErrorManagerContract.UiState = ApiErrorManagerContract.UiState(),
        val notifierManagerState: NotifierManagerContract.UiState = NotifierManagerContract.UiState(),
        val isOnline: Boolean = true,
        val themeMode: ThemeMode = ThemeMode.System,
        val photoLayout: PhotoLayout = PhotoLayout.Masonry,
        val photoColumns: Int? = null,
        val screenLayout: ScreenLayout = ScreenLayout.Auto,

        /** Language of the app; `null` follows the device. */
        val languageTag: String? = null,

        /** `false` until the saved settings are read; the values above are defaults until then. */
        val isSettingsLoaded: Boolean = false
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data class ApiErrorManagerEvent(val event: ApiErrorManagerContract.UiEvent) : UiEvent
        data class NotifierManagerEvent(val event: NotifierManagerContract.UiEvent) : UiEvent
    }
}

@Composable
fun RootGraphUi(
    modifier: Modifier = Modifier,
    component: RootGraphComponent
) {
    val uiState = component.getUiState()
    val obtainUiEvent = component::obtainUiEvent

    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val stack by component.stack.subscribeAsState()

    // The sign-in screen means there is no session: the images of the last user go with the rest of the data.
    ClearImageCacheEffect(isCleared = stack.active.instance is Child.Auth)

    BlurHost(
        modifier = modifier
            .fillMaxSize()
            .background(appColor.basic.background)
    ) {
        Children(
            stack = component.stack,
            modifier = Modifier
                .fillMaxSize()
                .blurSource(),
            animation = stackAnimation { fade() + scale() }
        ) { child ->
            CompositionLocalProvider(
                LocalPhotoGridMode provides uiState.photoLayout.toGridMode(),
                LocalPhotoGridColumns provides uiState.photoColumns,
                LocalWindowTypeOverride provides uiState.screenLayout.toWindowType(),
                LocalIsOnline provides uiState.isOnline
            ) {
                when (val instance = child.instance) {
                    is Child.Splash -> SplashUi(component = instance.component)
                    is Child.Main -> MainGraphUi(component = instance.component)
                    is Child.Photo -> PhotoUi(component = instance.component)
                    is Child.Profile -> ProfileUi(component = instance.component)
                    is Child.Auth -> AuthUi(component = instance.component)
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopCenter),
            visible = !uiState.isOnline,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(appColor.basic.error)
                    .statusBarsPadding()
                    .padding(appPadding.small),
                text = stringResource(Res.string.error_no_internet),
                style = appTypography.h6.medium,
                color = appColor.text.reversed,
                textAlign = TextAlign.Center
            )
        }

        ApiErrorManagerUi(
            uiState = uiState.apiErrorManagerState,
            isOffline = !uiState.isOnline,
            obtainUiEvent = { obtainUiEvent(RootGraphContract.UiEvent.ApiErrorManagerEvent(it)) }
        )

        NotifierManagerUi(
            uiState = uiState.notifierManagerState,
            obtainUiEvent = { obtainUiEvent(RootGraphContract.UiEvent.NotifierManagerEvent(it)) }
        )
    }
}