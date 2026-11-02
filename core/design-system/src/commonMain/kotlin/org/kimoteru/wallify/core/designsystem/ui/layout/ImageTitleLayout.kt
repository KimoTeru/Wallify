package org.kimoteru.wallify.core.designsystem.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withColor
import org.kimoteru.wallify.core.designsystem.ui.button.DefaultTextIconButton
import org.kimoteru.wallify.core.designsystem.ui.image.CachedAsyncImage
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf

@Composable
fun ImageTitleLayout(
    modifier: Modifier = Modifier,
    imageRes: Int,
    title: String,
    subtitle: String? = null,
    isContentCenter: Boolean = false,
    primaryButtonText: String? = null,
    primaryButtonColor: Color = AppTheme.color.basic.button.primary,
    primaryButtonEnabled: Boolean = true,
    primaryButtonTextStyle: TextStyle = AppTheme.typography.h5.semiBold.withColor(AppTheme.color.text.reversed),
    primaryButtonIcon: @Composable (() -> Unit)? = null,
    onPrimaryClick: (() -> Unit)? = null
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isContentCenter) Arrangement.Center else Arrangement.Top
    ) {
        CachedAsyncImage(
            modifier = Modifier.fillMaxWidth(),
            model = imageRes,
            contentScale = ContentScale.FillWidth
        )

        Text(
            modifier = Modifier.padding(top = appPadding.largeSpecial),
            text = title,
            style = appTypography.h4.semiBold
        )

        subtitle?.let {
            Text(
                modifier = Modifier.applyIf(!isContentCenter) {
                    padding(top = appPadding.medium)
                },
                text = it,
                style = if (isContentCenter) appTypography.h5.regular else appTypography.h6.regular,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )
        }

        if (primaryButtonText != null && onPrimaryClick != null) {
            DefaultTextIconButton(
                modifier = Modifier
                    .padding(vertical = appPadding.largeSpecial)
                    .fillMaxWidth(),
                text = primaryButtonText,
                backgroundColor = primaryButtonColor,
                textStyle = primaryButtonTextStyle,
                enabled = primaryButtonEnabled,
                icon = primaryButtonIcon,
                onClick = onPrimaryClick
            )
        }
    }
}