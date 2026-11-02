package org.kimoteru.wallify.core.designsystem.ui.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull

/** Rounded, optionally clickable background for an icon or a small piece of content. */
@Composable
fun Modifier.iconContainer(
    color: Color = AppTheme.color.basic.primary.copy(alpha = AppTheme.alpha.smallSpecial),
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(AppTheme.padding.small),
    shape: RoundedCornerShape = RoundedCornerShape(AppTheme.corner.mediumSpecial)
): Modifier =
    this
        .clip(shape)
        .applyIfNotNull {
            onClick?.let {
                debouncedClickable(onClick = it)
            }
        }
        .background(color)
        .padding(padding)