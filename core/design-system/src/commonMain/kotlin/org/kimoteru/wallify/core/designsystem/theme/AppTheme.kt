package org.kimoteru.wallify.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppAlphas
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppBlurs
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppBorderWidths
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppColors
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppCorners
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppDurationAnim
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppElevations
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppGradient
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppPaddings
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppRatios
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppScales
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppSizes
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppTypography
import org.kimoteru.wallify.core.designsystem.theme.compositions.AppZIndex
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

object AppTheme {
    val color: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val gradient: AppGradient
        @Composable
        @ReadOnlyComposable
        get() = LocalGradients.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val corner: AppCorners
        @Composable
        @ReadOnlyComposable
        get() = LocalCorners.current

    val padding: AppPaddings
        @Composable
        @ReadOnlyComposable
        get() = LocalPaddings.current

    val elevation: AppElevations
        @Composable
        @ReadOnlyComposable
        get() = LocalElevations.current

    val alpha: AppAlphas
        @Composable
        @ReadOnlyComposable
        get() = LocalAlphas.current

    val scale: AppScales
        @Composable
        @ReadOnlyComposable
        get() = LocalScales.current

    val borderWidth: AppBorderWidths
        @Composable
        @ReadOnlyComposable
        get() = LocalBorderWidths.current

    val durationAnim: AppDurationAnim
        @Composable
        @ReadOnlyComposable
        get() = LocalDurationAnim.current

    val size: AppSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalSizes.current

    val blur: AppBlurs
        @Composable
        @ReadOnlyComposable
        get() = LocalBlurs.current

    val ratio: AppRatios
        @Composable
        @ReadOnlyComposable
        get() = LocalRatios.current

    val zIndex: AppZIndex
        @Composable
        @ReadOnlyComposable
        get() = LocalZIndex.current
}