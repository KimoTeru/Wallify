package org.kimoteru.wallify.core.designsystem.ui.full_screen_loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.animation.progress.DefaultCircularProgressIndicator
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIf

@Composable
fun DefaultFullScreenLoader(
    modifier: Modifier = Modifier,
    showBackground: Boolean = false
) {
    val appSize = AppTheme.size
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color

    Box(
        modifier = modifier
            .applyIf(showBackground) {
                background(appColor.basic.dark.copy(alpha = appAlpha.largeSpecial))
            }
            .debouncedClickable(enabled = false, animateClick = false),
        contentAlignment = Alignment.Center
    ) {
        DefaultCircularProgressIndicator(
            modifier = Modifier.size(appSize.progressIndicator.sizeLarge),
            strokeWidth = appSize.progressIndicator.strokeWidthLarge
        )
    }
}