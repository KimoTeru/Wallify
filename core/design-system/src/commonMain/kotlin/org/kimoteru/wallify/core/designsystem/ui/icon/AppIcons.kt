package org.kimoteru.wallify.core.designsystem.ui.icon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
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