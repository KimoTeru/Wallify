package org.kimoteru.wallify.core.designsystem.ui.text

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import org.kimoteru.wallify.core.common.extensions.Bullet
import org.kimoteru.wallify.core.common.extensions.Star
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withColor
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf

@Composable
fun TextIcon(
    modifier: Modifier = Modifier,
    title: String,
    style: TextStyle = AppTheme.typography.h4.bold,
    isTextWeighted: Boolean = false,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    leftRow: @Composable (() -> Unit)? = null,
    rightRow: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment
    ) {
        leftRow?.invoke()

        Text(
            modifier = Modifier.applyIf(isTextWeighted) {
                weight(1f)
            },
            text = title,
            style = style
        )

        rightRow?.invoke()
    }
}

@Composable
fun TextBullet(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = AppTheme.typography.h5.regular,
    textAlign: TextAlign = TextAlign.Center,
    verticalAlignment: Alignment.Vertical = Alignment.Top
) {
    val appPadding = AppTheme.padding

    TextIcon(
        modifier = modifier,
        title = text,
        style = style,
        verticalAlignment = verticalAlignment,
        leftRow = {
            Text(
                modifier = Modifier.padding(end = appPadding.smallSpecial),
                text = String.Bullet,
                style = style,
                textAlign = textAlign
            )
        }
    )
}

@Composable
fun TextStar(
    modifier: Modifier = Modifier,
    style: TextStyle = AppTheme.typography.h5.bold.withColor(AppTheme.color.basic.error),
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        modifier = modifier,
        text = String.Star,
        style = style,
        textAlign = textAlign
    )
}