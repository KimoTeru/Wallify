package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateColor
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcon

/** Destination of the main navigation. */
@Immutable
data class GlassNavItem(
    val icon: ImageVector,
    val label: String
)

/** Floating capsule at the bottom of a phone screen. The screen reserves the room under its content. */
@Composable
fun GlassDock(
    modifier: Modifier = Modifier,
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    Row(
        modifier = modifier
            .height(appSize.navigation.dockHeight)
            .glassSurface(GlassLevel.Floating, CircleShape)
            .padding(horizontal = appPadding.small),
        horizontalArrangement = Arrangement.spacedBy(appPadding.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            GlassNavIcon(
                icon = item.icon,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

/** Floating vertical capsule at the edge of a tablet screen; [header] sits above the destinations. */
@Composable
fun GlassRail(
    modifier: Modifier = Modifier,
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    header: (@Composable ColumnScope.() -> Unit)? = null
) {
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    Column(
        modifier = modifier
            .width(appSize.navigation.railWidth)
            .glassSurface(GlassLevel.Floating, RoundedCornerShape(appCorner.large + appCorner.small))
            .padding(vertical = appPadding.mediumSpecial),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appPadding.small)
    ) {
        header?.invoke(this)

        items.forEachIndexed { index, item ->
            GlassNavIcon(
                icon = item.icon,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

/** Permanent navigation panel of a large window: title on top, destinations as labeled rows. */
@Composable
fun GlassDrawer(
    modifier: Modifier = Modifier,
    title: String,
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    Column(
        modifier = modifier
            .width(appSize.navigation.drawerWidth)
            .glassSurface(GlassLevel.Standard, RoundedCornerShape(appCorner.large + appCorner.smallSpecial))
            .padding(appPadding.mediumSpecial),
        verticalArrangement = Arrangement.spacedBy(appPadding.smallExtra)
    ) {
        Text(
            modifier = Modifier.padding(
                start = appPadding.mediumSpecial,
                top = appPadding.small,
                bottom = appPadding.medium
            ),
            text = title,
            style = appTypography.h4.bold,
            color = appColor.text.title
        )

        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val background = animateColor(
                if (isSelected) appColor.basic.primary.withAlpha(appAlpha.small) else Color.Transparent
            )
            val contentColor = animateColor(if (isSelected) appColor.text.title else appColor.text.subtitle)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(background)
                    .debouncedClickable(onClick = { onSelect(index) })
                    .padding(horizontal = appPadding.mediumSpecial, vertical = appPadding.mediumSpecial),
                horizontalArrangement = Arrangement.spacedBy(appPadding.mediumSpecial),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(icon = item.icon, tint = contentColor, size = appSize.icon.mediumSpecial)

                Text(
                    text = item.label,
                    style = appTypography.h6.semiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GlassNavIcon(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color
    val appSize = AppTheme.size

    val background = animateColor(
        if (selected) appColor.basic.primary.withAlpha(appAlpha.small) else Color.Transparent
    )
    val tint = animateColor(if (selected) appColor.text.title else appColor.text.subtitle)

    Box(
        modifier = Modifier
            .size(appSize.navigation.dockItemSize)
            .clip(CircleShape)
            .background(background)
            .debouncedClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon = icon, tint = tint, size = appSize.icon.medium)
    }
}