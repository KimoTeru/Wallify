package org.kimoteru.wallify.core.designsystem.ui.checkbox

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.common.extensions.half
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateAlpha
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateColor
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable

@Composable
fun CircleCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    size: Dp = AppTheme.size.checkbox.size,
    checkedColor: Color = AppTheme.color.basic.primary,
    uncheckedColor: Color = AppTheme.color.icons.nav,
    borderWidth: Dp = AppTheme.size.checkbox.borderWidth,
    innerPadding: Dp = AppTheme.size.checkbox.innerPadding
) {
    val appAlpha = AppTheme.alpha
    val appDurationAnim = AppTheme.durationAnim
    val appPadding = AppTheme.padding

    val color = animateColor(
        targetValue = if (checked) checkedColor else uncheckedColor,
        animationSpec = tween(appDurationAnim.smallExtra)
    )

    val root = modifier
        .size(size)
        .padding(appPadding.smallExtra.half)
        .alpha(if (enabled) appAlpha.full else appAlpha.largeSpecial)
        .clip(CircleShape)
        .border(borderWidth, color, CircleShape)
        .let {
            if (onCheckedChange != null) {
                it.debouncedClickable(animateClick = false) {
                    onCheckedChange(!checked)
                }
            } else it
        }

    Box(modifier = root, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .alpha(animateAlpha(visible = checked, animationSpec = tween(appDurationAnim.smallExtra)))
                .matchParentSize()
                .clip(CircleShape)
                .background(checkedColor)

        )
    }
}