package org.kimoteru.wallify.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppColors
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppDurationAnim
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppTypography
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalAlphas
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalBlurs
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalBorderWidths
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalColors
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalCorners
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalDurationAnim
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalElevations
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalGradients
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalPaddings
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalRatios
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalScales
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalSizes
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalTypography
import org.kimoteru.wallify.core.designsystem.theme.compositions.LocalZIndex

@Composable
fun AppTheme(
    animEnabled: Boolean = true,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) AppColors.DarkColors else AppColors.LightColors

    val durationAnim = remember(animEnabled) {
        if (animEnabled) AppDurationAnim() else AppDurationAnim.AnimOff
    }

    val fontFamily = rememberAppFontFamily()

    val typography = remember(colors, fontFamily) {
        AppTypography(colors.text.title, fontFamily)
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalGradients provides AppTheme.gradient,
        LocalTypography provides typography,
        LocalCorners provides AppTheme.corner,
        LocalPaddings provides AppTheme.padding,
        LocalDurationAnim provides durationAnim,
        LocalAlphas provides AppTheme.alpha,
        LocalBorderWidths provides AppTheme.borderWidth,
        LocalSizes provides AppTheme.size,
        LocalScales provides AppTheme.scale,
        LocalElevations provides AppTheme.elevation,
        LocalBlurs provides AppTheme.blur,
        LocalRatios provides AppTheme.ratio,
        LocalZIndex provides AppTheme.zIndex
    ) {
        content()
    }
}

@Composable
fun LightAppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalColors provides AppColors.LightColors) {
        content()
    }
}

@Composable
fun DarkAppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalColors provides AppColors.DarkColors) {
        content()
    }
}