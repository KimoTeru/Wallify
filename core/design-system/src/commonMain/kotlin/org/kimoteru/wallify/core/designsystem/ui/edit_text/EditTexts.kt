package org.kimoteru.wallify.core.designsystem.ui.edit_text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import org.kimoteru.wallify.core.common.constants.Constants
import org.kimoteru.wallify.core.common.extensions.isOne
import org.kimoteru.wallify.core.common.extensions.times2
import org.kimoteru.wallify.core.designsystem.extensions.times2
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf

@Composable
fun DefaultTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    hint: String,
    minLines: Int = Constants.DEFAULT_TEXT_FIELD_MIN_LINES,
    maxLines: Int = Constants.DEFAULT_TEXT_FIELD_MAX_LINES,
    maxLength: Int = Int.MAX_VALUE,
    textStyle: TextStyle = AppTheme.typography.h5.regular,
    backgroundColor: Color = AppTheme.color.basic.card.basic,
    shape: RoundedCornerShape = CircleShape,
    icons: @Composable (RowScope.() -> Unit)? = null,
    leadingIcon: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified),
    keyboardActions: KeyboardActions = KeyboardActions(),
    paddingValues: PaddingValues = PaddingValues(
        start = AppTheme.padding.small, end = AppTheme.padding.smallSpecial,
        top = AppTheme.padding.medium, bottom = AppTheme.padding.medium
    ),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    filterInput: (TextFieldValue) -> TextFieldValue = { it }
) {
    val appSize = AppTheme.size
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    var isFocused by rememberSaveable { mutableStateOf(false) }
    var localValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(value) }

    // Don't overwrite localValue from outside while focused — prevents IME reset
    LaunchedEffect(value.text, isFocused) {
        if (!isFocused || value.text.isEmpty()) {
            if (value.text != localValue.text) localValue = value.copy(composition = null)
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = appPadding.small)
                .applyIf(maxLines.isOne) {
                    height(appSize.icon.medium + appPadding.medium.times2)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke(this)

            BasicTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(paddingValues)
                    .onFocusChanged { isFocused = it.isFocused || it.hasFocus },
                value = localValue,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                onValueChange = { new ->
                    val applied = filterInput(new).limitTo(maxLength)
                    localValue = applied
                    onValueChange(applied)
                },
                enabled = enabled,
                maxLines = maxLines,
                minLines = minLines,
                visualTransformation = visualTransformation,
                textStyle = textStyle,
                decorationBox = { innerTextField ->
                    if (localValue.text.isEmpty()) {
                        Text(
                            text = hint,
                            style = textStyle.copy(color = appColor.text.subtitle)
                        )
                    }
                    innerTextField()
                },
                cursorBrush = SolidColor(appColor.text.title)
            )

            icons?.invoke(this)
        }
    }
}

private fun TextFieldValue.limitTo(max: Int): TextFieldValue {
    if (text.length <= max) return this
    val t = text.take(max)
    return copy(text = t, selection = TextRange(t.length), composition = null)
}