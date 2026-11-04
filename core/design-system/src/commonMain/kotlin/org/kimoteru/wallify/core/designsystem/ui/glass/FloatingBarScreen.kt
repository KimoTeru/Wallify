package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha

/**
 * Screen with a list that scrolls under a bar (title, chips, search) floating over its top edge. The bar fades into
 * the list through a scrim, hides while the list scrolls down and returns at the first scroll up, together with the
 * navigation dock ([LocalNavigationVisibility]).
 *
 * [bar] must bring its own top margin (status bar included, see [rememberScreenPadding]). [content] gets the height
 * the list has to keep free above its first item as `topPadding`.
 */
@Composable
fun FloatingBarScreen(
    modifier: Modifier = Modifier,
    bar: @Composable () -> Unit,
    content: @Composable (topPadding: Dp) -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding

    val density = LocalDensity.current
    val visibility = LocalNavigationVisibility.current
    var barHeight by remember { mutableStateOf(Dp.Hairline) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberHideOnScrollConnection(visibility))
    ) {
        content(barHeight + appPadding.medium)

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopCenter),
            visible = visibility.isVisible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                appColor.basic.background.withAlpha(appAlpha.largeExtra),
                                appColor.basic.transparent
                            )
                        )
                    )
                    .onSizeChanged { size -> barHeight = with(density) { size.height.toDp() } }
            ) {
                bar()
            }
        }
    }
}