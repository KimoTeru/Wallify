package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/**
 * Space that floating navigation (dock or rail) takes over the screen content. The navigation host provides it,
 * screens read it through [rememberScreenPadding] so lists scroll under the dock and stop clear of it at the end.
 */
val LocalNavigationPadding = compositionLocalOf { PaddingValues() }

/** Margins of a screen: its own gutter plus the system bars and the floating navigation. */
@Immutable
data class ScreenPadding(
    val start: Dp,
    val top: Dp,
    val end: Dp,
    val bottom: Dp
) {
    /** Margins of a header placed above a list: no bottom margin, the list brings its own. */
    val header: PaddingValues
        get() = PaddingValues(start = start, top = top, end = end)

    /** Margins of everything inside one scrolling container. */
    val full: PaddingValues
        get() = PaddingValues(start = start, top = top, end = end, bottom = bottom)

    /** Margins of a list under a header: [gap] separates it from the header. */
    fun list(gap: Dp): PaddingValues = PaddingValues(start = start, top = gap, end = end, bottom = bottom)
}

@Composable
fun rememberScreenPadding(gutter: Dp = AppTheme.padding.medium): ScreenPadding {
    val layoutDirection = LocalLayoutDirection.current
    val navigation = LocalNavigationPadding.current
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    return ScreenPadding(
        start = gutter + navigation.calculateStartPadding(layoutDirection),
        top = gutter + systemBars.calculateTopPadding() + navigation.calculateTopPadding(),
        end = gutter + navigation.calculateEndPadding(layoutDirection),
        bottom = gutter + systemBars.calculateBottomPadding() + navigation.calculateBottomPadding()
    )
}