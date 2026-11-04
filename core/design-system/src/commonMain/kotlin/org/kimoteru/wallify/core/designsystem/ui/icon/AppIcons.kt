package org.kimoteru.wallify.core.designsystem.ui.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme

/**
 * Icons drawn in code as [ImageVector], so they need no resource files and work on every target.
 * Paths use a 24x24 viewport. The fill color is replaced by the tint of [Icon].
 */
object AppIcons {

    /** Chevron pointing left. */
    val Back: ImageVector by lazy {
        icon("Back") {
            moveTo(15.41f, 7.41f)
            lineTo(14f, 6f)
            lineTo(8f, 12f)
            lineTo(14f, 18f)
            lineTo(15.41f, 16.59f)
            lineTo(10.83f, 12f)
            close()
        }
    }

    /** Cross. */
    val Close: ImageVector by lazy {
        icon("Close") {
            moveTo(19f, 6.41f)
            lineTo(17.59f, 5f)
            lineTo(12f, 10.59f)
            lineTo(6.41f, 5f)
            lineTo(5f, 6.41f)
            lineTo(10.59f, 12f)
            lineTo(5f, 17.59f)
            lineTo(6.41f, 19f)
            lineTo(12f, 13.41f)
            lineTo(17.59f, 19f)
            lineTo(19f, 17.59f)
            lineTo(13.41f, 12f)
            close()
        }
    }

    val Search: ImageVector by lazy {
        icon(
            "Search",
            "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16" +
                "c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 " +
                "9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
        )
    }

    val Tune: ImageVector by lazy {
        icon(
            "Tune",
            "M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7" +
                "h4V5h-4V3h-2v6z"
        )
    }

    val GridView: ImageVector by lazy {
        icon(
            "GridView",
            "M3 3v8h8V3H3zm6 6H5V5h4v4zm-6 4v8h8v-8H3zm6 6H5v-4h4v4zm4-16v8h8V3h-8zm6 6h-4V5h4v4zm-6 4v8h8v-8h-8z" +
                "m6 6h-4v-4h4v4z"
        )
    }

    val AutoAwesome: ImageVector by lazy {
        icon(
            "AutoAwesome",
            "M19 9l1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25L19 9zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 " +
                "20l2.5-5.5L17 12l-5.5-2.5zM19 15l-1.25 2.75L15 19l2.75 1.25L19 23l1.25-2.75L23 19l-2.75-1.25L19 15z"
        )
    }

    val Shuffle: ImageVector by lazy {
        icon(
            "Shuffle",
            "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5z" +
                "m.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z"
        )
    }

    val Camera: ImageVector by lazy {
        icon(
            "Camera",
            "M12 8.8a3.2 3.2 0 1 0 0 6.4 3.2 3.2 0 0 0 0-6.4zM9 2L7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 " +
                "2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z"
        )
    }

    val Settings: ImageVector by lazy {
        icon(
            "Settings",
            "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-" +
                ".22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-" +
                ".43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08" +
                ".47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12." +
                "22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l." +
                "36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-" +
                "1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"
        )
    }

    val Download: ImageVector by lazy {
        icon("Download", "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z")
    }

    val Check: ImageVector by lazy {
        icon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
    }

    val Add: ImageVector by lazy {
        icon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
    }

    val Location: ImageVector by lazy {
        icon(
            "Location",
            "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5" +
                "s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"
        )
    }

    val Person: ImageVector by lazy {
        icon(
            "Person",
            "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33" +
                "-4-8-4z"
        )
    }

    val DarkMode: ImageVector by lazy {
        icon(
            "DarkMode",
            "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0" +
                "-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z"
        )
    }

    val LightMode: ImageVector by lazy {
        icon(
            "LightMode",
            "M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55zm7.45 3.91l-" +
                "1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM20 10.5v2h3" +
                "v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm-1 16.95h2V19.5h-2v2.95zm-7.45-3." +
                "91l1.41 1.41 1.79-1.8-1.41-1.41-1.79 1.8z"
        )
    }

    val Visibility: ImageVector by lazy {
        icon(
            "Visibility",
            "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-" +
                "2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1." +
                "34-3-3-3z"
        )
    }

    val Bookmark: ImageVector by lazy {
        icon("Bookmark", "M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z")
    }

    val BookmarkBorder: ImageVector by lazy {
        icon(
            "BookmarkBorder",
            "M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2zm0 15l-5-2.18L7 18V5h10v13z"
        )
    }

    val OpenInNew: ImageVector by lazy {
        icon(
            "OpenInNew",
            "M19 19H5V5h7V3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 " +
                "1.41 1.41L19 6.41V10h2V3h-7z"
        )
    }

    val Logout: ImageVector by lazy {
        icon(
            "Logout",
            "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8" +
                "v-2H4V5z"
        )
    }

    val Refresh: ImageVector by lazy {
        icon(
            "Refresh",
            "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08" +
                "c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"
        )
    }

    val ChevronRight: ImageVector by lazy {
        icon("ChevronRight", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z")
    }

    val Edit: ImageVector by lazy {
        icon(
            "Edit",
            "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-" +
                "1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
        )
    }

    val Upload: ImageVector by lazy {
        icon("Upload", "M9 16h6v-6h4l-7-7-7 7h4v6zm-4 2h14v2H5v-2z")
    }

    val Language: ImageVector by lazy {
        icon(
            "Language",
            "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm6.93 6h-2.95c-." +
                "32-1.25-.78-2.45-1.38-3.56 1.84.63 3.37 1.91 4.33 3.56zM12 4.04c.83 1.2 1.48 2.53 1.91 3.96h-3.82c." +
                "43-1.43 1.08-2.76 1.91-3.96zM4.26 14C4.1 13.36 4 12.69 4 12s.1-1.36.26-2h3.38c-.08.66-.14 1.32-.14 " +
                "2 0 .68.06 1.34.14 2H4.26zm.82 2h2.95c.32 1.25.78 2.45 1.38 3.56-1.84-.63-3.37-1.9-4.33-3.56zm2.95-" +
                "8H5.08c.96-1.66 2.49-2.93 4.33-3.56C8.81 5.55 8.35 6.75 8.03 8zM12 19.96c-.83-1.2-1.48-2.53-1.91-3." +
                "96h3.82c-.43 1.43-1.08 2.76-1.91 3.96zM14.34 14H9.66c-.09-.66-.16-1.32-.16-2 0-.68.07-1.35.16-2h4.6" +
                "8c.09.65.16 1.32.16 2 0 .68-.07 1.34-.16 2zm.25 5.56c.6-1.11 1.06-2.31 1.38-3.56h2.95c-.96 1.65-2.4" +
                "9 2.93-4.33 3.56zM16.36 14c.08-.66.14-1.32.14-2 0-.68-.06-1.34-.14-2h3.38c.16.64.26 1.31.26 2s-.1 1" +
                ".36-.26 2h-3.38z"
        )
    }

    /** Horizontal bar: minimizes a window. */
    val Minimize: ImageVector by lazy {
        icon("Minimize", "M19 13H5v-2h14v2z")
    }

    /** Two overlapping squares: restores a maximized window. */
    val Restore: ImageVector by lazy {
        icon(
            "Restore",
            "M3 5H1v16c0 1.1.9 2 2 2h16v-2H3V5zm18-4H7c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V3c0-1.1-." +
                "9-2-2-2zm0 16H7V3h14v14z"
        )
    }

    val CropLandscape: ImageVector by lazy {
        icon(
            "CropLandscape",
            "M19 5H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 12H5V7h14v10z"
        )
    }

    val CropPortrait: ImageVector by lazy {
        icon(
            "CropPortrait",
            "M17 3H7c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H7V5h10v14z"
        )
    }

    val CropSquare: ImageVector by lazy {
        icon(
            "CropSquare",
            "M18 4H6c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 14H6V6h12v12z"
        )
    }

    private fun icon(name: String, pathData: String): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black)
    ).build()

    private inline fun icon(
        name: String,
        pathBuilder: PathBuilder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black), pathBuilder = pathBuilder).build()
}

/** Any icon of [AppIcons]; the size follows the modifier, the default is the medium icon size. */
@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color = AppTheme.color.text.title,
    size: Dp = AppTheme.size.icon.medium
) {
    Icon(
        modifier = modifier.size(size),
        imageVector = icon,
        contentDescription = null,
        tint = tint
    )
}

@Composable
fun IconBack(
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.color.basic.primary
) {
    Icon(
        modifier = modifier,
        imageVector = AppIcons.Back,
        contentDescription = null,
        tint = tint
    )
}

@Composable
fun IconClose(
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.color.basic.white
) {
    Icon(
        modifier = modifier,
        imageVector = AppIcons.Close,
        contentDescription = null,
        tint = tint
    )
}