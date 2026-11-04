package org.kimoteru.wallify.core.designsystem.ui.switch

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import org.kimoteru.wallify.core.common.extensions.half
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable

@Composable
fun DefaultSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true
) {
    val appSize = AppTheme.size
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding

    val transition = updateTransition(checked)

    val thumbOffset by transition.animateDp { isChecked ->
        if (isChecked) appSize.switch.trackWidth - appSize.switch.thumbSize - appPadding.smallExtra.half else appPadding.smallExtra.half
    }

    val trackColor by transition.animateColor { isChecked ->
        if (isChecked) appColor.basic.primary else appColor.icons.nav
    }

    // The primary color is white on the night scheme, so the thumb on the active track takes the reversed text color.
    val thumbColor by transition.animateColor { isChecked ->
        if (isChecked) appColor.text.reversed else appColor.basic.white
    }

    val alpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f)

    val clickableModifier = if (onCheckedChange != null) {
        Modifier.debouncedClickable(animateClick = false) {
            onCheckedChange.invoke(!checked)
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .width(appSize.switch.trackWidth)
            .height(appSize.switch.trackHeight)
            .clip(RoundedCornerShape(appCorner.medium))
            .background(trackColor)
            .alpha(alpha),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(appSize.switch.thumbSize)
                .background(thumbColor, CircleShape)
        )
    }
}