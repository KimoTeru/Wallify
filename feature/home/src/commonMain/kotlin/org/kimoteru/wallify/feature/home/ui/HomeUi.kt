package org.kimoteru.wallify.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.presentation.base.BaseContract
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.app_name

object HomeContract {
    @Immutable
    class UiState : BaseContract.UiState

    sealed interface UiEvent : BaseContract.UiEvent
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
    val appTypography = AppTheme.typography

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = appTypography.h1.bold,
            color = appColor.text.title
        )
    }
}