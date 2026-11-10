package org.kimoteru.wallify.feature.root.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurHost
import org.kimoteru.wallify.core.designsystem.ui.image.ClearImageCacheEffect
import org.kimoteru.wallify.core.model.settings.SupportedLanguages
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.settings.PhotoLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ScreenLayoutUi
import org.kimoteru.wallify.core.presentation.settings.ThemeModeUi
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerUi
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent.Child

object RootGraphContract {
    @Immutable
    data class UiState(
        val notifierManagerState: NotifierManagerContract.UiState = NotifierManagerContract.UiState(),
        val isOnline: Boolean = true,
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

@Composable
fun RootGraphUi(
    modifier: Modifier = Modifier,
    component: RootGraphComponent
) {
    val uiState = component.getUiState()
    val obtainUiEvent = component::obtainUiEvent

    val appColor = AppTheme.color

    val stack by component.stack.subscribeAsState()

    // The sign-in screen means there is no session: the images of the last user go with the rest of the data.
    ClearImageCacheEffect(isCleared = stack.active.instance is Child.Auth)

    BlurHost(
        modifier = modifier
            .fillMaxSize()
            .background(appColor.basic.background)
    ) {
        RootGraphStack(
            modifier = Modifier.fillMaxSize(),
            component = component,
            uiState = uiState
        )

        NotifierManagerUi(
            uiState = uiState.notifierManagerState,
            obtainUiEvent = { obtainUiEvent(RootGraphContract.UiEvent.NotifierManagerEvent(it)) }
        )
    }
}