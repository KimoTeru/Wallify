package org.kimoteru.wallify.feature.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.presentation.base.BaseContract

private const val SPLASH_DURATION_MS = 1_500L

sealed interface SplashContract {
    class UiState : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent {
        data object EndAnimation : UiEvent
    }
}

@Preview
@Composable
fun PreviewSplashUi() {
    AppTheme {
        SplashUi(
            component = object : SplashComponent {
                @Composable
                override fun getUiState(): SplashContract.UiState = SplashContract.UiState()
            }
        )
    }
}

@Composable
fun SplashUi(
    modifier: Modifier = Modifier,
    component: SplashComponent
) {
    val obtainEvent = component::obtainUiEvent

    val appColors = AppTheme.color.basic
    val appTypography = AppTheme.typography

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        obtainEvent(SplashContract.UiEvent.EndAnimation)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(appColors.primary, appColors.background)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Wallify",
            style = appTypography.h1.bold,
            color = appColors.white
        )
    }
}