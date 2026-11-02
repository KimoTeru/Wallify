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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.blur.BlurHost
import org.kimoteru.wallify.core.designsystem.ui.blur.blurSource
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerContract
import org.kimoteru.wallify.core.presentation.components.units.api_error_manager.presentation.ui.ApiErrorManagerUi
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerContract
import org.kimoteru.wallify.core.presentation.components.units.notifier_manager.presentation.ui.NotifierManagerUi
import org.kimoteru.wallify.feature.home.ui.HomeUi
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent
import org.kimoteru.wallify.feature.root.presentation.components.RootGraphComponent.Child
import org.kimoteru.wallify.feature.splash.ui.SplashUi

object RootGraphContract {
    @Immutable
    data class UiState(
        val apiErrorManagerState: ApiErrorManagerContract.UiState = ApiErrorManagerContract.UiState(),
        val notifierManagerState: NotifierManagerContract.UiState = NotifierManagerContract.UiState(),
        val isOnline: Boolean = true
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
            when (val instance = child.instance) {
                is Child.Splash -> SplashUi(component = instance.component)
                is Child.Home -> HomeUi(component = instance.component)
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
                text = "No internet connection",
                style = appTypography.h6.medium,
                color = appColor.basic.white,
                textAlign = TextAlign.Center
            )
        }

        ApiErrorManagerUi(
            uiState = uiState.apiErrorManagerState,
            obtainUiEvent = { obtainUiEvent(RootGraphContract.UiEvent.ApiErrorManagerEvent(it)) }
        )

        NotifierManagerUi(
            uiState = uiState.notifierManagerState,
            obtainUiEvent = { obtainUiEvent(RootGraphContract.UiEvent.NotifierManagerEvent(it)) }
        )
    }
}