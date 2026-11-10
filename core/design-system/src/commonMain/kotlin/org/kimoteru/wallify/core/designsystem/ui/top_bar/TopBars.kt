package org.kimoteru.wallify.core.designsystem.ui.top_bar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.icon.IconBack
import org.kimoteru.wallify.core.designsystem.ui.icon.IconClose
import org.kimoteru.wallify.core.designsystem.ui.icon.iconContainer

/** Top bar with an optional back button on the left and close button on the right. */
@Composable
fun DefaultTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleStyle: TextStyle = AppTheme.typography.h5.semiBold,
    titleMaxLines: Int = Int.MAX_VALUE,
    paddingValues: PaddingValues = PaddingValues(
        top = AppTheme.padding.smallSpecial + AppTheme.padding.smallExtra.half,
        bottom = AppTheme.padding.smallExtra.half
    ),
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    val appColor = AppTheme.color

    DefaultTopBar(
        modifier = modifier,
        title = title,
        titleStyle = titleStyle,
        titleMaxLines = titleMaxLines,
        paddingValues = paddingValues,
        actionsLeft = {
            onBack?.let { callback ->
                IconBack(modifier = Modifier.iconContainer(onClick = callback))
            }
        },
        actionsRight = {
            actions()
            onClose?.let { callback ->
                IconClose(
                    modifier = Modifier.iconContainer(onClick = callback),
                    tint = appColor.basic.primary
                )
            }
        }
    )
}

/** Title sits after the left actions and fills the space up to the right ones. */
@Composable
fun DefaultTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleStyle: TextStyle = AppTheme.typography.h5.semiBold,
    titleMaxLines: Int = Int.MAX_VALUE,
    paddingValues: PaddingValues = PaddingValues(
        top = AppTheme.padding.smallSpecial + AppTheme.padding.smallExtra.half,
        bottom = AppTheme.padding.smallExtra.half
    ),
    actionsLeft: @Composable RowScope.() -> Unit = {},
    actionsRight: @Composable RowScope.() -> Unit = {}
) {
    val appPadding = AppTheme.padding

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actionsLeft()

        if (title != null) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = appPadding.small),
                text = title,
                style = titleStyle,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        actionsRight()
    }
}