package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcon

/** Look of a [GlassButton]: glass capsule, solid inverted capsule for the main action, ghost for quiet ones. */
enum class GlassButtonStyle {
    Glass,
    Solid,
    Ghost
}

@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    text: String,
    style: GlassButtonStyle = GlassButtonStyle.Glass,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    level: GlassLevel = GlassLevel.Elevated,
    onClick: () -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appBorderWidth = AppTheme.borderWidth
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val contentColor = when (style) {
        GlassButtonStyle.Glass -> appColor.text.title
        GlassButtonStyle.Solid -> appColor.text.reversed
        GlassButtonStyle.Ghost -> appColor.text.subtitle
    }

    Row(
        modifier = modifier
            .alpha(if (enabled) appAlpha.full else appAlpha.medium)
            .clip(CircleShape)
            .then(
                when (style) {
                    GlassButtonStyle.Glass -> Modifier.glassSurface(level, CircleShape)
                    GlassButtonStyle.Solid -> Modifier.background(appColor.basic.button.primary)
                    GlassButtonStyle.Ghost -> Modifier.border(appBorderWidth.small, appColor.glass.edge, CircleShape)
                }
            )
            .debouncedClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = appPadding.largeSpecial, vertical = appPadding.mediumSpecial),
        horizontalArrangement = Arrangement.spacedBy(appPadding.small, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { AppIcon(icon = it, tint = contentColor, size = appSize.icon.mediumSpecial) }

        Text(
            text = text,
            style = appTypography.h6.semiBold,
            color = contentColor
        )
    }
}

/** Round glass button with an icon, for actions on top of photos and in bars. */
@Composable
fun GlassIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color = AppTheme.color.text.title,
    level: GlassLevel = GlassLevel.Elevated,
    onClick: () -> Unit
) {
    val appSize = AppTheme.size

    Box(
        modifier = modifier
            .size(appSize.control.iconButton)
            .glassSurface(level = level, shape = CircleShape)
            .debouncedClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon = icon, tint = tint, size = appSize.icon.mediumSpecial)
    }
}