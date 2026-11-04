package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val basic: BasicColors,
    val icons: IconColors,
    val text: TextColors,
    val glass: GlassColors
) {
    companion object {
        val LightColors = AppColors(
            basic = BasicColors(
                background = Color(0xFFF4F7FF),
                primary = Color(0xFF10131C),
                white = Color(0xFFFFFFFF),
                black = Color(0xFF000000),
                dark = Color(0xFF0B0E17),
                transparent = Color(0x00000000),
                card = CardColors(
                    basic = Color(0xFFFFFFFF),
                    onSurface = Color(0xFFFFFFFF),
                    reversed = Color(0xFF1C1F29)
                ),
                error = Color(0xFFBA1A1A),
                button = ButtonColors(
                    primary = Color(0xFF10131C),
                    secondary = Color(0xFFE3E8F5)
                )
            ),
            icons = IconColors(
                care = Color(0xFF5C6477),
                nav = Color(0xFFB4BBCB)
            ),
            text = TextColors(
                title = Color(0xFF10131C),
                subtitle = Color(0xFF5C6477),
                common = Color(0xFF1C1F29),
                fieldType = Color(0xFF10131C),
                fieldHint = Color(0xFF8C93A3),
                reversed = Color(0xFFFFFFFF),
                onReversed = Color(0xFFFFFFFF)
            ),
            // On the light background white glass alone would vanish, so recessed panes are tinted with the ink
            // color, cards are almost opaque white, and the hairline is an ink one on every side.
            glass = GlassColors(
                sub = Color(0x1210131C),
                standard = Color(0xD9FFFFFF),
                elevated = Color(0xF2FFFFFF),
                hover = Color(0xFFFFFFFF),
                edge = Color(0x2410131C),
                edgeTop = Color(0x3310131C),
                dock = Color(0xD9FFFFFF),
                solid = Color(0xFFFFFFFF),
                scrim = Color(0x8010131C),
                divider = Color(0x1A10131C)
            )
        )

        val DarkColors = AppColors(
            basic = BasicColors(
                background = Color(0xFF0B0E17),
                primary = Color(0xFFFFFFFF),
                white = Color(0xFFFFFFFF),
                black = Color(0xFF000000),
                dark = Color(0xFF0B0E17),
                transparent = Color(0x00000000),
                card = CardColors(
                    basic = Color(0xFF181B25),
                    onSurface = Color(0xFF1C1F29),
                    reversed = Color(0xFFFFFFFF)
                ),
                error = Color(0xFFFFB4AB),
                button = ButtonColors(
                    primary = Color(0xFFFFFFFF),
                    secondary = Color(0xFF272A34)
                )
            ),
            icons = IconColors(
                care = Color(0xFFA1A8B8),
                nav = Color(0xFF444B5C)
            ),
            text = TextColors(
                title = Color(0xFFFFFFFF),
                subtitle = Color(0xFFA1A8B8),
                common = Color(0xFFE0E2EF),
                fieldType = Color(0xFFFFFFFF),
                fieldHint = Color(0xFF646B7C),
                reversed = Color(0xFF0B0E17),
                onReversed = Color(0xFF0B0E17)
            ),
            glass = GlassColors(
                sub = Color(0x0AFFFFFF),
                standard = Color(0x14FFFFFF),
                elevated = Color(0x24FFFFFF),
                hover = Color(0x2EFFFFFF),
                edge = Color(0x1FFFFFFF),
                edgeTop = Color(0x4DFFFFFF),
                dock = Color(0xB31C1F29),
                solid = Color(0xFF1C1F29),
                scrim = Color(0x990B0E17),
                divider = Color(0x0FFFFFFF)
            )
        )
    }
}

data class BasicColors(
    val background: Color,
    val primary: Color,
    val white: Color,
    val black: Color,
    val dark: Color,
    val transparent: Color,
    val card: CardColors,
    val error: Color,
    val button: ButtonColors
)

data class CardColors(
    val basic: Color,
    val onSurface: Color,
    val reversed: Color
)

data class ButtonColors(
    val primary: Color,
    val secondary: Color
)

data class TextColors(
    val title: Color,
    val subtitle: Color,
    val common: Color,
    val fieldType: Color,
    val fieldHint: Color,
    val reversed: Color,
    val onReversed: Color
)

data class IconColors(
    val care: Color,
    val nav: Color
)

/**
 * Translucent layers of the crystalline surfaces. [sub], [standard], [elevated] and [hover] are the fills of the
 * glass levels, [edge] and [edgeTop] draw the hairline border with a brighter top, [dock] tints the frosted
 * (blurred) bars that are drawn over content, [solid] fills sheets and dialogs that must not show what is behind them,
 * [scrim] dims the blurred screen under them (dark on both schemes, so a light dialog stands out on the day one too),
 * [divider] separates rows inside a glass container.
 */
data class GlassColors(
    val sub: Color,
    val standard: Color,
    val elevated: Color,
    val hover: Color,
    val edge: Color,
    val edgeTop: Color,
    val dock: Color,
    val solid: Color,
    val scrim: Color,
    val divider: Color
)

val LocalColors = staticCompositionLocalOf { AppColors.DarkColors }