package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcon
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons

/**
 * Search capsule: submits with the keyboard search action, shows a clear button while it holds text.
 * Over scrolling content use [GlassLevel.Floating], so the text stays readable.
 */
@Composable
fun GlassSearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    level: GlassLevel = GlassLevel.Standard,
    trailing: (@Composable () -> Unit)? = null
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val textStyle = appTypography.h6.medium.copy(color = appColor.text.fieldType)

    Row(
        modifier = modifier
            .height(appSize.control.height)
            .glassSurface(level, CircleShape)
            .padding(horizontal = appPadding.mediumSpecial),
        horizontalArrangement = Arrangement.spacedBy(appPadding.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(icon = AppIcons.Search, tint = appColor.text.subtitle, size = appSize.icon.mediumSpecial)

        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    style = textStyle,
                    color = appColor.text.fieldHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = textStyle,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                cursorBrush = SolidColor(appColor.text.title)
            )
        }

        if (value.isNotEmpty()) {
            AppIcon(
                modifier = Modifier.debouncedClickable(onClick = onClear),
                icon = AppIcons.Close,
                tint = appColor.text.subtitle,
                size = appSize.icon.mediumSpecial
            )
        }

        trailing?.invoke()
    }
}