package org.kimoteru.wallify.core.designsystem.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import org.kimoteru.wallify.core.common.response.ResponseError
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassButton
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorDescription
import org.kimoteru.wallify.core.designsystem.ui.network.networkErrorTitle
import org.kimoteru.wallify.core.resources.generated.resources.Res
import org.kimoteru.wallify.core.resources.generated.resources.common_retry

/**
 * Failed load of a screen or a list that has nothing to show: the text by the type of [error] with its HTTP status
 * code and «Повторить». Takes the whole content of the screen; when there is content, the failure goes to the
 * snackbar instead.
 */
@Composable
fun ErrorContentLayout(
    modifier: Modifier = Modifier,
    error: ResponseError?,
    onRetry: () -> Unit
) {
    ContentMessageLayout(
        modifier = modifier,
        title = networkErrorTitle(error),
        description = networkErrorDescription(error),
        actionText = stringResource(Res.string.common_retry),
        onAction = onRetry
    )
}

/**
 * Centered message on the whole content of a screen with an optional action: a failed load ([ErrorContentLayout]) or
 * an empty list (only [title]).
 */
@Composable
fun ContentMessageLayout(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appPadding.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appPadding.medium, Alignment.CenterVertically)
    ) {
        Text(
            text = title,
            style = appTypography.h4.semiBold,
            color = appColor.text.title,
            textAlign = TextAlign.Center
        )

        description?.let {
            Text(
                text = it,
                style = appTypography.h6.regular,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )
        }

        if (actionText != null && onAction != null) {
            GlassButton(text = actionText, onClick = onAction)
        }
    }
}