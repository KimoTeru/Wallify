package org.kimoteru.wallify.core.designsystem.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

@Composable
fun InfoLine(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    titleColor: Color = AppTheme.color.text.title
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = appTypography.h6.regular,
            color = titleColor
        )

        Text(
            modifier = Modifier.padding(start = appPadding.mediumSpecial),
            text = subtitle,
            style = appTypography.h6.medium,
            color = appColor.text.title,
            textAlign = TextAlign.End
        )
    }
}