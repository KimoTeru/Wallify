package org.kimoteru.wallify.core.designsystem.ui.badge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.icon.iconContainer

@Composable
fun DefaultBadge(
    modifier: Modifier = Modifier,
    count: Int
) {
    val appColor = AppTheme.color
    val appTypography = AppTheme.typography

    Box(
        modifier = modifier.iconContainer(
            color = appColor.basic.primary,
            shape = CircleShape,
            padding = PaddingValues()
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = appTypography.h7.medium,
            color = appColor.text.reversed,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}