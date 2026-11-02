package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val basic: BasicColors,
    val icons: IconColors,
    val text: TextColors
) {
    companion object {
        val LightColors = AppColors(
            basic = BasicColors(
                background = Color(0xFFF1F3F6),
                primary = Color(0xFFFF945B),
                white = Color(0xFFFFFFFF),
                black = Color(0xFF000000),
                dark = Color(0xFF1F1F27),
                transparent = Color(0x00000000),
                card = CardColors(
                    basic = Color(0xFFFFFFFF),
                    onSurface = Color(0xFFFFFFFF),
                    reversed = Color(0xFF2C2C34)
                ),
                error = Color(0xFFED685C),
                button = ButtonColors(
                    primary = Color(0xFFFF945B),
                    secondary = Color(0xFFE0E5EE)
                )
            ),
            icons = IconColors(
                care = Color(0xFFA9B6C8),
                nav = Color(0xFFC5D1DB)
            ),
            text = TextColors(
                title = Color(0xFF242424),
                subtitle = Color(0xFF91A0B6),
                common = Color(0xFF212121),
                fieldType = Color(0xFF242424),
                fieldHint = Color(0xFFC3CBD7),
                reversed = Color(0xFFFFFFFF),
                onReversed = Color(0xFFFFFFFF)
            )
        )

        val DarkColors = AppColors(
            basic = BasicColors(
                background = Color(0xFF1F1F27),
                primary = Color(0xFFFF945B),
                white = Color(0xFFFFFFFF),
                black = Color(0xFF000000),
                dark = Color(0xFF1F1F27),
                transparent = Color(0x00000000),
                card = CardColors(
                    basic = Color(0xFF2C2C34),
                    onSurface = Color(0xFF1F1F27),
                    reversed = Color(0xFFFFFFFF)
                ),
                error = Color(0xFFED685C),
                button = ButtonColors(
                    primary = Color(0xFFFF945B),
                    secondary = Color(0xFF0D0D15)
                )
            ),
            icons = IconColors(
                care = Color(0xFF9B9B9B),
                nav = Color(0xFF6E6E73)
            ),
            text = TextColors(
                title = Color(0xFFFFFFFF),
                subtitle = Color(0xFF9B9B9B),
                common = Color(0xFFE3E3E5),
                fieldType = Color(0xFFE3E3E5),
                fieldHint = Color(0xFF626266),
                reversed = Color(0xFFFFFFFF),
                onReversed = Color(0xFF242424)
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

val LocalColors = staticCompositionLocalOf { AppColors.DarkColors }