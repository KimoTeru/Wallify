package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/** Tile with a value and its small uppercase caption, e.g. the number of downloads. */
@Composable
fun GlassStat(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    Column(
        modifier = modifier
            .glassSurface(GlassLevel.Sub, RoundedCornerShape(appCorner.medium))
            .padding(horizontal = appPadding.small, vertical = appPadding.mediumSpecial),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appPadding.smallExtra, Alignment.CenterVertically)
    ) {
        Text(
            text = value,
            style = appTypography.h4.bold,
            color = appColor.text.title,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = label.uppercase(),
            style = appTypography.h8.semiBold,
            color = appColor.text.subtitle,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}