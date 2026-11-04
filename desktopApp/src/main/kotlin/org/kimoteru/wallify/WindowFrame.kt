package org.kimoteru.wallify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import java.awt.Cursor
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.theme.withAlpha
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcon
import org.kimoteru.wallify.core.designsystem.ui.icon.AppIcons

/**
 * Frame of a window without the system decorations (Windows, Linux): the app draws its own title bar with the
 * window buttons in the colors of the theme, the title bar drags the window and a double click maximizes it. A window
 * without a frame has no edges to grab, so [WindowResizeHandles] bring them back.
 * [minSize] is the smallest the window can be resized to.
 */
@Composable
internal fun FrameWindowScope.WindowFrame(
    windowState: WindowState,
    title: String,
    minSize: DpSize,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    val appBorderWidth = AppTheme.borderWidth
    val appColor = AppTheme.color

    val isMaximized = windowState.placement == WindowPlacement.Maximized
    val toggleMaximize = {
        windowState.placement = if (isMaximized) WindowPlacement.Floating else WindowPlacement.Maximized
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColor.basic.background)
            .border(appBorderWidth.small, appColor.glass.edge)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WindowDraggableArea(
                modifier = Modifier.pointerInput(isMaximized) {
                    detectTapGestures(onDoubleTap = { toggleMaximize() })
                }
            ) {
                WindowTitleBar(
                    title = title,
                    isMaximized = isMaximized,
                    onMinimize = { windowState.isMinimized = true },
                    onToggleMaximize = toggleMaximize,
                    onClose = onClose
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                content()
            }
        }

        if (!isMaximized) {
            WindowResizeHandles(
                window = window,
                minWidth = minSize.width.value.toInt(),
                minHeight = minSize.height.value.toInt()
            )
        }
    }
}

@Composable
private fun WindowTitleBar(
    title: String,
    isMaximized: Boolean,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appSize = AppTheme.size
    val appTypography = AppTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(appSize.window.titleBarHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = appPadding.medium),
            text = title,
            style = appTypography.h7.medium,
            color = appColor.text.subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        WindowButton(icon = AppIcons.Minimize, onClick = onMinimize)

        WindowButton(
            icon = if (isMaximized) AppIcons.Restore else AppIcons.CropSquare,
            onClick = onToggleMaximize
        )

        WindowButton(icon = AppIcons.Close, isClose = true, onClick = onClose)
    }
}

@Composable
private fun WindowButton(
    icon: ImageVector,
    isClose: Boolean = false,
    onClick: () -> Unit
) {
    val appAlpha = AppTheme.alpha
    val appColor = AppTheme.color
    val appSize = AppTheme.size

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val background = when {
        isHovered && isClose -> appColor.basic.error
        isHovered -> appColor.basic.primary.withAlpha(appAlpha.small)
        else -> Color.Transparent
    }
    val tint = if (isHovered && isClose) appColor.text.reversed else appColor.text.title

    Box(
        modifier = Modifier
            .width(appSize.window.titleButtonWidth)
            .fillMaxHeight()
            .background(background)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(
            icon = icon,
            tint = tint,
            size = appSize.icon.small
        )
    }
}

/** Part of the window border or corner that resizes the window in the given directions. */
private enum class ResizeSide(
    val cursor: Int,
    val left: Boolean = false,
    val right: Boolean = false,
    val top: Boolean = false,
    val bottom: Boolean = false
) {
    Top(Cursor.N_RESIZE_CURSOR, top = true),
    Bottom(Cursor.S_RESIZE_CURSOR, bottom = true),
    Left(Cursor.W_RESIZE_CURSOR, left = true),
    Right(Cursor.E_RESIZE_CURSOR, right = true),
    TopLeft(Cursor.NW_RESIZE_CURSOR, left = true, top = true),
    TopRight(Cursor.NE_RESIZE_CURSOR, right = true, top = true),
    BottomLeft(Cursor.SW_RESIZE_CURSOR, left = true, bottom = true),
    BottomRight(Cursor.SE_RESIZE_CURSOR, right = true, bottom = true)
}

/** Invisible strips along the edges and in the corners; the corners are drawn last, so they win over the edges. */
@Composable
private fun BoxScope.WindowResizeHandles(
    window: java.awt.Window,
    minWidth: Int,
    minHeight: Int
) {
    val appSize = AppTheme.size

    val edge = appSize.window.resizeEdge
    val corner = appSize.window.resizeCorner

    ResizeHandle(
        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(edge),
        window = window, side = ResizeSide.Top, minWidth = minWidth, minHeight = minHeight
    )
    ResizeHandle(
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(edge),
        window = window, side = ResizeSide.Bottom, minWidth = minWidth, minHeight = minHeight
    )
    ResizeHandle(
        modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().width(edge),
        window = window, side = ResizeSide.Left, minWidth = minWidth, minHeight = minHeight
    )
    ResizeHandle(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(edge),
        window = window, side = ResizeSide.Right, minWidth = minWidth, minHeight = minHeight
    )
    ResizeHandle(
        modifier = Modifier.align(Alignment.TopStart).size(corner),
        window = window, side = ResizeSide.TopLeft, minWidth = minWidth, minHeight = minHeight
    )
    ResizeHandle(
        modifier = Modifier.align(Alignment.TopEnd).size(corner),
        window = window, side = ResizeSide.TopRight, minWidth = minWidth, minHeight = minHeight
    )
    ResizeHandle(
        modifier = Modifier.align(Alignment.BottomStart).size(corner),
        window = window, side = ResizeSide.BottomLeft, minWidth = minWidth, minHeight = minHeight
    )
    ResizeHandle(
        modifier = Modifier.align(Alignment.BottomEnd).size(corner),
        window = window, side = ResizeSide.BottomRight, minWidth = minWidth, minHeight = minHeight
    )
}

/**
 * Resizes [window] while dragged. The pointer is read in screen coordinates: a handle on the left or top edge moves
 * together with the window, so coordinates relative to the handle would make the window jitter.
 */
@Composable
private fun ResizeHandle(
    modifier: Modifier,
    window: java.awt.Window,
    side: ResizeSide,
    minWidth: Int,
    minHeight: Int
) {
    Box(
        modifier = modifier
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(side.cursor)))
            .pointerInput(side, minWidth, minHeight) {
                var startBounds = Rectangle()
                var startPointer = Point()

                detectDragGestures(
                    onDragStart = {
                        startBounds = window.bounds
                        startPointer = MouseInfo.getPointerInfo().location
                    },
                    onDrag = { change, _ ->
                        change.consume()

                        val pointer = MouseInfo.getPointerInfo().location
                        window.bounds = startBounds.resized(
                            side = side,
                            dx = pointer.x - startPointer.x,
                            dy = pointer.y - startPointer.y,
                            minWidth = minWidth,
                            minHeight = minHeight
                        )
                    }
                )
            }
    )
}

private fun Rectangle.resized(side: ResizeSide, dx: Int, dy: Int, minWidth: Int, minHeight: Int): Rectangle {
    val result = Rectangle(this)

    if (side.right) {
        result.width = maxOf(minWidth, width + dx)
    }

    if (side.bottom) {
        result.height = maxOf(minHeight, height + dy)
    }

    if (side.left) {
        val newWidth = maxOf(minWidth, width - dx)
        result.x = x + width - newWidth
        result.width = newWidth
    }

    if (side.top) {
        val newHeight = maxOf(minHeight, height - dy)
        result.y = y + height - newHeight
        result.height = newHeight
    }

    return result
}