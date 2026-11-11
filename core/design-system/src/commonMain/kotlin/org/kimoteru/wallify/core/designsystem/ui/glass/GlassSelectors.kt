package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateColor
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcon
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf

/**
 * Pebble chip: glass when idle, solid inverted when [selected]. [leading] is drawn before the text. Over photos pass
 * [GlassLevel.Floating] as [level], so the chip is dense enough to stay readable.
 */
@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean = false,
    icon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    level: GlassLevel = GlassLevel.Standard,
    onClick: () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val contentColor = animateColor(if (selected) appColor.text.reversed else appColor.text.common)

    Row(
        modifier = modifier
            .height(appSize.control.chipHeight)
            .applyIf(selected) { isSelected ->
                if (isSelected) {
                    clip(CircleShape).background(appColor.basic.button.primary)
                } else {
                    glassSurface(level, CircleShape)
                }
            }
            .debouncedClickable(onClick = onClick)
            .padding(horizontal = appPadding.mediumSpecial),
        horizontalArrangement = Arrangement.spacedBy(appPadding.small, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()

        icon?.let { AppIcon(icon = it, tint = contentColor, size = appSize.icon.small) }

        Text(
            text = text,
            style = appTypography.h7.medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Capsule with equal segments. The selected fill slides from one segment to the next. */
@Composable
fun GlassSegmentedControl(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val appColor = AppTheme.color
    val appDurationAnim = AppTheme.durationAnim
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    if (options.isEmpty()) return

    val gap = appPadding.smallExtra
    val safeIndex = selectedIndex.coerceIn(0, options.lastIndex)

    BoxWithConstraints(
        modifier = modifier
            .glassSurface(GlassLevel.Sub, CircleShape)
            .padding(gap)
    ) {
        val transition = updateTransition(safeIndex)
        val segmentWidth = (maxWidth - gap * options.lastIndex) / options.size
        val indicatorOffset by transition.animateDp(
            transitionSpec = { tween(appDurationAnim.small) }
        ) { index ->
            (segmentWidth + gap) * index
        }

        // The offset changes on every frame of the slide: read in the layout phase, it composes nothing again.
        Box(
            modifier = Modifier
                .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
                .width(segmentWidth)
                .height(appSize.control.chipHeight)
                .clip(CircleShape)
                .background(appColor.basic.button.primary)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            options.forEachIndexed { index, option ->
                val contentColor by transition.animateColor(
                    transitionSpec = { tween(appDurationAnim.small) }
                ) { selected ->
                    if (index == selected) appColor.text.reversed else appColor.text.subtitle
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(appSize.control.chipHeight)
                        .clip(CircleShape)
                        .debouncedClickable(onClick = { onSelect(index) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = appPadding.small),
                        text = option,
                        style = appTypography.h7.semiBold,
                        color = contentColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}