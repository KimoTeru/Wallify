package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

data class AppSizes(
    val icon: IconSize = IconSize(),
    val bottomSheet: BottomSheetSizeSize = BottomSheetSizeSize(),
    val switch: SwitchSize = SwitchSize(),
    val progressIndicator: ProgressIndicatorSize = ProgressIndicatorSize(),
    val divider: DividerSize = DividerSize(),
    val checkbox: CheckboxSize = CheckboxSize(),
    val pageIndicator: PageIndicatorSize = PageIndicatorSize()
)

data class IconSize(
    val smallExtra: Dp = 8.dp,
    val smallSpecial: Dp = 12.dp,
    val small: Dp = 16.dp,
    val mediumSpecial: Dp = 22.dp,
    val medium: Dp = 24.dp,
    val largeSpecial: Dp = 32.dp,
    val large: Dp = 44.dp
)

data class BottomSheetSizeSize(
    val drag: DpSize = DpSize(36.dp, 6.dp)
)

data class SwitchSize(
    val trackWidth: Dp = 52.dp,
    val trackHeight: Dp = 32.dp,
    val thumbSize: Dp = 28.dp
)

data class ProgressIndicatorSize(
    val sizeMedium: Dp = 24.dp,
    val sizeLarge: Dp = 48.dp,
    val strokeWidthSmall: Dp = 2.dp,
    val strokeWidthLarge: Dp = 4.dp
)

data class DividerSize(
    val thickness: Dp = 1.dp,
    val dashLength: Dp = 6.dp,
    val gapLength: Dp = 4.dp
)

data class CheckboxSize(
    val size: Dp = 24.dp,
    val borderWidth: Dp = 2.dp,
    val innerPadding: Dp = borderWidth * 2
)

data class PageIndicatorSize(
    val defaultRadius: Dp = 6.dp,
    val selectedLength: Dp = defaultRadius * 2
)

val LocalSizes = staticCompositionLocalOf { AppSizes() }