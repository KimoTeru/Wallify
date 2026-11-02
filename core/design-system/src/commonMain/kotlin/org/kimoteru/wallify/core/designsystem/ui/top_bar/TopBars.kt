package org.kimoteru.wallify.core.designsystem.ui.top_bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.icon.IconBack
import org.kimoteru.wallify.core.designsystem.ui.icon.IconClose
import org.kimoteru.wallify.core.designsystem.ui.icon.iconContainer
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNull

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

/** Title with optional actions on both sides. The title stays centered and never overlaps the actions. */
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
    actionsLeft: @Composable() (RowScope.() -> Unit) = {},
    actionsRight: @Composable() (RowScope.() -> Unit) = {}
) {
    val density = LocalDensity.current
    val appPadding = AppTheme.padding

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues)
    ) {
        var rightWidth by remember { mutableStateOf<Dp?>(null) }
        var leftWidth by remember { mutableStateOf<Dp?>(null) }
        var isTitleOverflow by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .applyIfNull(leftWidth) {
                    onSizeChanged {
                        leftWidth = with(density) { it.width.toDp() }
                    }
                }
        ) {
            actionsLeft()
        }

        title?.let { nonNullTitle ->
            Text(
                modifier = Modifier
                    .applyIfNotNull {
                        rightWidth?.let { rWidth ->
                            leftWidth?.let { lWidth ->
                                if (isTitleOverflow) {
                                    padding(
                                        start = lWidth + appPadding.medium,
                                        end = rWidth + appPadding.medium
                                    )
                                } else {
                                    padding(horizontal = maxOf(lWidth, rWidth) + appPadding.medium)
                                }
                            }
                        }
                    }
                    .align(Alignment.Center),
                text = nonNullTitle,
                style = titleStyle,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                onTextLayout = { textLayoutResult ->
                    isTitleOverflow = textLayoutResult.hasVisualOverflow
                }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .applyIfNull(rightWidth) {
                    onSizeChanged {
                        rightWidth = with(density) { it.width.toDp() }
                    }
                }
        ) {
            actionsRight()
        }
    }
}