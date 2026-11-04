package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/**
 * Labeled input of a form on glass. The label sits above the field, as in the other glass components the field is a
 * sunken panel ([GlassLevel.Sub]) with a rounded rectangle shape. [maxLines] above one makes a multiline field
 * (about, bio).
 */
@Composable
fun GlassTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    val textStyle = appTypography.h6.medium.copy(color = appColor.text.fieldType)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(appPadding.small)
    ) {
        Text(
            text = label.uppercase(),
            style = appTypography.caps.semiBold,
            color = appColor.text.subtitle
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = appSize.control.height)
                .glassSurface(GlassLevel.Sub, RoundedCornerShape(appCorner.medium))
                .padding(horizontal = appPadding.mediumSpecial, vertical = appPadding.medium),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = maxLines == 1,
                minLines = minLines,
                maxLines = maxLines,
                textStyle = textStyle,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(appColor.text.title)
            )
        }
    }
}