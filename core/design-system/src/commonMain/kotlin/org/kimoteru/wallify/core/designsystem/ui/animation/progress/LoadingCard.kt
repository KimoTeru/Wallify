package org.kimoteru.wallify.core.designsystem.ui.animation.progress

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.animation.compose.animateColor
import org.kimoteru.wallify.core.designsystem.ui.card.DefaultRowCard

@Composable
fun DefaultLoadingCard(modifier: Modifier = Modifier) {
    var isAnimating by remember { mutableStateOf(false) }
    val appColor = AppTheme.color
    val appDurationAnim = AppTheme.durationAnim
    val backgroundColor =
        animateColor(
            if (isAnimating) appColor.icons.care else appColor.basic.card.basic,
            animationSpec = infiniteRepeatable(
                animation = tween(appDurationAnim.medium),
                repeatMode = RepeatMode.Reverse
            )
        )

    LaunchedEffect(Unit) {
        isAnimating = true
    }

    DefaultRowCard(
        modifier = modifier,
        backgroundColor = backgroundColor
    )
}