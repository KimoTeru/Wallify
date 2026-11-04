package org.kimoteru.wallify.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButtonStyle
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassCard
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassLevel
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.auth_cancel
import org.kimoteru.wallify.core.resources.generated.resources.auth_description
import org.kimoteru.wallify.core.resources.generated.resources.auth_sign_in
import org.kimoteru.wallify.core.resources.generated.resources.auth_title
import org.kimoteru.wallify.core.resources.generated.resources.auth_waiting_for_browser

object AuthContract {
    @Immutable
    data class UiState(
        val isSigningIn: Boolean = false
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object SignIn : UiEvent
        data object CancelSignIn : UiEvent
    }
}

@Preview
@Composable
fun PreviewAuthUi() {
    AppTheme {
        AuthUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : AuthComponent {
                @Composable
                override fun getUiState(): AuthContract.UiState = AuthContract.UiState()
            }
        )
    }
}

@Composable
fun AuthUi(
    modifier: Modifier = Modifier,
    component: AuthComponent
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(listOf(appColor.glass.standard, appColor.basic.transparent))
            )
            .systemBarsPadding()
            .padding(appPadding.medium),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.widthIn(max = appSize.panel.dialogWidth),
            level = GlassLevel.Standard,
            corners = appCorner.large + appCorner.medium
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(appPadding.medium)
            ) {
                Text(
                    text = stringResource(Res.string.auth_title),
                    style = appTypography.h2.bold,
                    color = appColor.text.title,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(Res.string.auth_description),
                    style = appTypography.h5.regular,
                    color = appColor.text.subtitle,
                    textAlign = TextAlign.Center
                )

                if (uiState.isSigningIn) {
                    Text(
                        text = stringResource(Res.string.auth_waiting_for_browser),
                        style = appTypography.h6.medium,
                        color = appColor.text.common,
                        textAlign = TextAlign.Center
                    )

                    GlassButton(
                        text = stringResource(Res.string.auth_cancel),
                        style = GlassButtonStyle.Ghost,
                        onClick = { obtainEvent(AuthContract.UiEvent.CancelSignIn) }
                    )
                } else {
                    GlassButton(
                        text = stringResource(Res.string.auth_sign_in),
                        style = GlassButtonStyle.Solid,
                        onClick = { obtainEvent(AuthContract.UiEvent.SignIn) }
                    )
                }
            }
        }
    }
}