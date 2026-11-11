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
    val pageIndicator: PageIndicatorSize = PageIndicatorSize(),
    val navigation: NavigationSize = NavigationSize(),
    val avatar: AvatarSize = AvatarSize(),
    val control: ControlSize = ControlSize(),
    val photoGrid: PhotoGridSize = PhotoGridSize(),
    val panel: PanelSize = PanelSize(),
    val chart: ChartSize = ChartSize(),
    val window: WindowSizes = WindowSizes()
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

/** Floating dock on phones, rail on tablets and permanent drawer on large windows. */
data class NavigationSize(
    val dockHeight: Dp = 64.dp,
    val dockItemSize: Dp = 48.dp,
    val dockInset: Dp = 20.dp,
    val railWidth: Dp = 64.dp,
    val drawerWidth: Dp = 220.dp
)

data class AvatarSize(
    val small: Dp = 24.dp,
    val medium: Dp = 32.dp,
    val large: Dp = 56.dp,
    val profile: Dp = 96.dp
)

data class ControlSize(
    val height: Dp = 48.dp,
    val chipHeight: Dp = 32.dp,
    val iconButton: Dp = 44.dp
)

/**
 * Columns of the photo grid per window width. The list layout has its own, fewer columns ([listColumns] on a phone,
 * [listWideColumns] on wider windows). [spacing] is the vertical gap; the horizontal gap is the screen gutter.
 */
data class PhotoGridSize(
    val compactColumns: Int = 2,
    val mediumColumns: Int = 3,
    val expandedColumns: Int = 4,
    val listColumns: Int = 1,
    val listWideColumns: Int = 2,
    val spacing: Dp = 12.dp
)

/**
 * Width of side panels (detail inspector, filter dialog) and the readable width of text screens.
 * [dialogMaxWidth] and [sheetMaxWidth] are the limits of Material 3 for dialogs and bottom sheets: wider windows
 * get a centered dialog or sheet of that width instead of one stretched over the whole window.
 */
data class PanelSize(
    val inspectorWidth: Dp = 320.dp,
    val dialogWidth: Dp = 480.dp,
    val dialogMaxWidth: Dp = 560.dp,
    val sheetMaxWidth: Dp = 640.dp,
    val contentMaxWidth: Dp = 640.dp
)

/**
 * Sizes of a desktop window. A typical width for each `WindowType`, safely inside its breakpoints, is what the
 * window takes when the user picks that screen layout in the settings; [minWidth] and [minHeight] are the
 * smallest the window can be dragged to. A forced layout needs room for its navigation (the rail, the drawer), so
 * with it the window cannot be narrower than [mediumMinWidth] or [expandedMinWidth]: below that the content of
 * the screens is squeezed into a few characters per line. The app draws its own title bar of [titleBarHeight]
 * (under the transparent system one on macOS, with window buttons [titleButtonWidth] wide elsewhere), and a
 * window without a system frame is resized by grabbing [resizeEdge] wide edges and [resizeCorner] corners.
 */
data class WindowSizes(
    val compactWidth: Dp = 400.dp,
    val mediumWidth: Dp = 720.dp,
    val expandedWidth: Dp = 1100.dp,
    val minWidth: Dp = 360.dp,
    val mediumMinWidth: Dp = 600.dp,
    val expandedMinWidth: Dp = 840.dp,
    val minHeight: Dp = 560.dp,
    val titleBarHeight: Dp = 32.dp,
    val titleButtonWidth: Dp = 46.dp,
    val resizeEdge: Dp = 6.dp,
    val resizeCorner: Dp = 14.dp
)

/**
 * [shareBarHeight] is the thickness of `ShareBar`, e.g. the share of one endpoint among all requests. `BarChart`,
 * e.g. requests per day for a month, is [barChartHeight] high with [barChartGap] between the bars; a zero value is
 * a stub of [barChartMinBarHeight], so every day of the row stays visible.
 */
data class ChartSize(
    val sparklineHeight: Dp = 56.dp,
    val shareBarHeight: Dp = 6.dp,
    val barChartHeight: Dp = 96.dp,
    val barChartGap: Dp = 2.dp,
    val barChartMinBarHeight: Dp = 2.dp
)

val LocalSizes = staticCompositionLocalOf { AppSizes() }