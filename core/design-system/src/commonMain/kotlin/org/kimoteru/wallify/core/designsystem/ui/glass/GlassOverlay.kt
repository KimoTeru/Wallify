package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.kimoteru.wallify.core.common.functional.runIfOrNull
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.dialog.DefaultDialogContainer
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfComposable

/**
 * Opaque glass-edged sheet over a blurred screen: a bottom sheet on phones ([isSheet]) and a centered dialog
 * elsewhere. Tapping outside dismisses it. [content] gets `close`, which plays the exit animation and then calls
 * [onDismiss]. Put it into [ScreenOverlay]: the root screen draws it above everything, the navigation included.
 * A sheet's glass runs under the system navigation bar, the content stays above it, and the bottom hairline is off.
 */
@Composable
fun GlassOverlay(
    modifier: Modifier = Modifier,
    isSheet: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(close: () -> Unit) -> Unit
) {
    val appColor = AppTheme.color
    val appCorner = AppTheme.corner
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    var dismissRequested by remember { mutableStateOf(false) }

    val edges = if (isSheet) GlassEdge.All - GlassEdge.Bottom else GlassEdge.All
    val shape = if (isSheet) {
        RoundedCornerShape(
            topStart = appCorner.large + appCorner.small,
            topEnd = appCorner.large + appCorner.small
        )
    } else {
        RoundedCornerShape(appCorner.large + appCorner.medium)
    }

    DefaultDialogContainer(
        onDismiss = onDismiss,
        dismissRequested = dismissRequested,
        windowInsets = runIfOrNull(isSheet) {
            WindowInsets.statusBars
        }
    ) {
        Column(
            modifier = modifier
                .align(if (isSheet) Alignment.BottomCenter else Alignment.Center)
                .applyIf(isSheet) { sheet ->
                    if (sheet) {
                        widthIn(max = appSize.panel.sheetMaxWidth).fillMaxWidth()
                    } else {
                        width(appSize.panel.dialogWidth)
                    }
                }
                .debouncedClickable(animateClick = false)
                .glassSurface(GlassLevel.Solid, shape, edges)
                .padding(horizontal = appPadding.medium, vertical = appPadding.small)
                .applyIfComposable(isSheet) { navigationBarsPadding() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSheet) {
                Box(
                    modifier = Modifier
                        .padding(bottom = appPadding.medium)
                        .size(appSize.bottomSheet.drag)
                        .clip(CircleShape)
                        .background(appColor.icons.care)
                )
            }

            content { dismissRequested = true }
        }
    }
}