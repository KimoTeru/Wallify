package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/**
 * The one snackbar of the app: an opaque glass card with the [message] and, when [actionLabel] and [onAction] are
 * given, a button (a repeat of the failed action). It is drawn over every screen, where nothing blurs under it, so
 * the glass is [GlassLevel.Solid]; on a wide window it is not wider than a dialog.
 */
@Composable
fun GlassSnackbar(
    modifier: Modifier = Modifier,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    Row(
        modifier = modifier
            .widthIn(max = appSize.panel.dialogMaxWidth)
            .fillMaxWidth()
            .glassSurface(GlassLevel.Solid, RoundedCornerShape(appCorner.large + appCorner.smallSpecial))
            .padding(horizontal = appPadding.medium, vertical = appPadding.mediumSpecial),
        horizontalArrangement = Arrangement.spacedBy(appPadding.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = message,
            style = appTypography.h6.medium,
            color = appColor.text.title
        )

        if (actionLabel != null && onAction != null) {
            GlassButton(
                text = actionLabel,
                style = GlassButtonStyle.Solid,
                onClick = onAction
            )
        }
    }
}