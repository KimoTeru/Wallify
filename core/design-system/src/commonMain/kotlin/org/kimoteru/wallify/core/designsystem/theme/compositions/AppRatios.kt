package org.kimoteru.wallify.core.designsystem.theme.compositions

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Proportions for sizing content, in two kinds:
 * - fractions of a 360 dp reference screen width, for `Modifier.fillMaxWidth(fraction)`;
 * - aspect ratios (width / height), for `Modifier.aspectRatio(ratio)`.
 *
 * Example:
 * ```
 * val appRatio = AppTheme.ratio
 *
 * AsyncImage(
 *     modifier = Modifier
 *         .fillMaxWidth(appRatio.card.width)
 *         .aspectRatio(appRatio.image.portrait),
 *     model = url,
 *     contentDescription = null
 * )
 * ```
 */
data class AppRatios(
    val card: CardRatio = CardRatio(),
    val image: ImageRatio = ImageRatio()
)

data class CardRatio(
    val width: Float = 160 / 360f
)

data class ImageRatio(
    val square: Float = 1f,
    val portrait: Float = 3 / 4f,
    val landscape: Float = 4 / 3f,
    val tall: Float = 9 / 16f,
    val wide: Float = 16 / 9f
)

val LocalRatios = staticCompositionLocalOf { AppRatios() }