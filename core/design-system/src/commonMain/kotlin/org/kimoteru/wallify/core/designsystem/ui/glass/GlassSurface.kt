package org.kimoteru.wallify.core.designsystem.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.blur.frostedBackdrop
import org.kimoteru.wallify.core.designsystem.ui.click.debouncedClickable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfComposable
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull

/**
 * Opacity step of a glass surface: recessed panes, regular cards and panels, raised controls, bars floating over
 * content ([Floating]: frosted, the content under it blurred, see `frostedBackdrop`) and sheets and dialogs
 * ([Solid], opaque).
 */
enum class GlassLevel {
    Sub,
    Standard,
    Elevated,
    Floating,
    Solid
}

/**
 * Crystalline surface: translucent fill and a hairline edge that is brighter on top, like light falling from above.
 * The shape clips the content too.
 */
@Composable
fun Modifier.glassSurface(
    level: GlassLevel = GlassLevel.Standard,
    shape: Shape = RoundedCornerShape(AppTheme.corner.large + AppTheme.corner.smallSpecial)
): Modifier {
    val appBorderWidth = AppTheme.borderWidth
    val appGlass = AppTheme.color.glass

    val fill = when (level) {
        GlassLevel.Sub -> appGlass.sub
        GlassLevel.Standard -> appGlass.standard
        GlassLevel.Elevated -> appGlass.elevated
        GlassLevel.Floating -> appGlass.dock
        GlassLevel.Solid -> appGlass.solid
    }

    return this
        .clip(shape)
        .applyIfComposable(level == GlassLevel.Floating) { frostedBackdrop() }
        .background(fill)
        .border(
            width = appBorderWidth.small,
            brush = Brush.verticalGradient(listOf(appGlass.edgeTop, appGlass.edge)),
            shape = shape
        )
}

/** Card on a glass surface. Clickable when [onClick] is set. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Standard,
    corners: Dp = AppTheme.corner.large + AppTheme.corner.smallSpecial,
    paddingValues: PaddingValues = PaddingValues(AppTheme.padding.largeSpecial),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glassSurface(level = level, shape = RoundedCornerShape(corners))
            .applyIfNotNull {
                onClick?.let { callback -> debouncedClickable(onClick = callback) }
            }
            .padding(paddingValues),
        content = content
    )
}