package org.kimoteru.wallify.core.designsystem.ui.selection

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha
import org.kimoteru.wallify.core.designsystem.ui.card.DefaultRowCard
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf

interface LabeledOption {
    val label: String
}

@Composable
fun <T> DefaultSelector(
    modifier: Modifier = Modifier,
    selectedItem: T? = null,
    items: List<T>,
    itemVerticalPadding: Dp = AppTheme.padding.mediumSpecial,
    itemShape: RoundedCornerShape? = null,
    itemTextStyle: TextStyle = AppTheme.typography.h5.medium,
    selectedColor: Color = AppTheme.color.basic.primary,
    unselectedColor: Color = AppTheme.color.basic.background,
    isEnableCornerErrorLine: Boolean = false,
    onItemClick: (T) -> Unit
) where T : LabeledOption {
    val appColor = AppTheme.color
    val appAlpha = AppTheme.alpha
    val appPadding = AppTheme.padding
    val appBorderWidth = AppTheme.borderWidth

    val finalShape = itemShape ?: CircleShape

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemVerticalPadding)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem

            DefaultRowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(isEnableCornerErrorLine) {
                        border(
                            width = appBorderWidth.small,
                            color = appColor.basic.error,
                            shape = finalShape
                        )
                    },
                shape = finalShape,
                onClick = { onItemClick(item) },
                horizontalArrangement = Arrangement.Start,
                backgroundColor =
                    if (isSelected) selectedColor.copy(alpha = appAlpha.smallSpecial)
                    else unselectedColor
            ) {
                DefaultCheckbox(selected = isSelected)

                Text(
                    modifier = Modifier.padding(start = appPadding.small),
                    text = item.label,
                    style = itemTextStyle,
                    color =
                        if (isSelected) selectedColor
                        else appColor.text.title
                )
            }
        }
    }
}

@Composable
private fun DefaultCheckbox(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null
) {
    val appSize = AppTheme.size
    val appColor = AppTheme.color
    val appAlpha = AppTheme.alpha

    RadioButton(
        modifier = modifier.size(appSize.icon.medium),
        selected = selected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(
            selectedColor = appColor.basic.primary,
            unselectedColor = appColor.icons.nav,
            disabledSelectedColor = appColor.basic.primary.withAlpha(appAlpha.medium),
            disabledUnselectedColor = appColor.icons.nav.withAlpha(appAlpha.medium)
        )
    )
}