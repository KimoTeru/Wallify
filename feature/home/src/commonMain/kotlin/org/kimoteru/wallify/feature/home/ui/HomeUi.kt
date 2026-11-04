package org.kimoteru.wallify.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.button.DefaultTextButton
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.app_name
import org.kimoteru.wallify.core.resources.generated.resources.auth_sign_out
import org.kimoteru.wallify.core.resources.generated.resources.home_signed_in_as

object HomeContract {
    @Immutable
    data class UiState(
        val userName: String? = null
    ) : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object SignOut : UiEvent
    }
}

@Preview
@Composable
fun PreviewHomeUi() {
    AppTheme {
        HomeUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : HomeComponent {
                @Composable
                override fun getUiState(): HomeContract.UiState = HomeContract.UiState()
            }
        )
    }
}

@Composable
fun HomeUi(
    modifier: Modifier = Modifier,
    component: HomeComponent
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appPadding.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = appTypography.h1.bold,
            color = appColor.text.title
        )

        uiState.userName?.let { userName ->
            Text(
                modifier = Modifier.padding(top = appPadding.medium),
                text = stringResource(Res.string.home_signed_in_as, userName),
                style = appTypography.h5.regular,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )
        }

        DefaultTextButton(
            modifier = Modifier.padding(top = appPadding.largeSpecial),
            text = stringResource(Res.string.auth_sign_out),
            onClick = { obtainEvent(HomeContract.UiEvent.SignOut) }
        )
    }
}