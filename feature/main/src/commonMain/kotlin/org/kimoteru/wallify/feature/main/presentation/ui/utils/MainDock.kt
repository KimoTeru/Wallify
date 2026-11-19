package org.kimoteru.wallify.feature.main.presentation.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassDock
import org.kimoteru.wallify.core.designsystem.ui.glass.GlassNavItem
import org.kimoteru.wallify.core.designsystem.ui.glass.NavigationVisibility

/**
 * The dock of a phone, hidden while the screens scroll down. [visibility] is read here and not in the navigation
 * host: it flips on every change of the scroll direction, and only the dock composes again, not the whole host.
 */
@Composable
internal fun MainDock(
    modifier: Modifier = Modifier,
    visibility: NavigationVisibility,
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size

    AnimatedVisibility(
        modifier = modifier,
        visible = visibility.isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        GlassDock(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(
                    start = appPadding.medium,
                    end = appPadding.medium,
                    bottom = appSize.navigation.dockInset
                ),
            items = items,
            selectedIndex = selectedIndex,
            onSelect = onSelect
        )
    }
}