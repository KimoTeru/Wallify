package org.kimoteru.wallify.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
import org.kimoteru.wallify.core.designsystem.ui.adaptive.LocalWindowTypeOverride
import org.kimoteru.wallify.core.designsystem.ui.adaptive.WindowType
import org.kimoteru.wallify.core.designsystem.ui.locale.ApplyAppLocale
import org.kimoteru.wallify.core.designsystem.ui.locale.LocalAppLocale
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridColumns
import org.kimoteru.wallify.core.designsystem.ui.photo.LocalPhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.photo.PhotoGridMode
import org.kimoteru.wallify.core.designsystem.ui.pull_to_refresh.LocalRefreshRequests

/**
 * The one global provider of the app: the tokens, the language ([languageTag], `null` keeps the default locale) and
 * the choices of the user on the whole app that the root passes in ([refreshRequests] are the keys of the desktop
 * window that refresh the list on screen). Nested providers are only for a part of the tree
 * (a forced scheme, the blur of a screen, the padding of the navigation).
 */
@Composable
fun AppTheme(
    animEnabled: Boolean = true,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    languageTag: String? = null,
    photoGridMode: PhotoGridMode = PhotoGridMode.Masonry,
    photoGridColumns: Int? = null,
    windowTypeOverride: WindowType? = null,
    refreshRequests: Flow<Unit> = emptyFlow(),
    content: @Composable () -> Unit
) {
    languageTag?.let {
        ApplyAppLocale(languageTag = it)
    }

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
        LocalZIndex provides AppTheme.zIndex,
        LocalAppLocale provides languageTag,
        LocalPhotoGridMode provides photoGridMode,
        LocalPhotoGridColumns provides photoGridColumns,
        LocalWindowTypeOverride provides windowTypeOverride,
        LocalRefreshRequests provides refreshRequests
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