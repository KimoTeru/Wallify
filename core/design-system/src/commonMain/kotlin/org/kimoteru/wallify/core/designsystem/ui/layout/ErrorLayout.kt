package org.kimoteru.wallify.core.designsystem.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import org.kimoteru.wallify.core.common.extensions.half
import org.kimoteru.wallify.core.common.functional.runIfOrNull
import org.kimoteru.wallify.core.designsystem.extensions.half
import org.kimoteru.wallify.core.designsystem.theme.AppTheme
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNotNull
import org.kimoteru.wallify.core.designsystem.ui.modifier.applyIfNull

@Composable
fun ErrorLayout(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    isContentCenter: Boolean = false
) {
    val appColor = AppTheme.color
    val appPadding = AppTheme.padding
    val appTypography = AppTheme.typography

    val density = LocalDensity.current
    var heightContent by remember {
        mutableStateOf<Dp?>(null)
    }

    BoxWithConstraints(
        modifier = modifier.padding(horizontal = appPadding.medium),
        contentAlignment = if (isContentCenter) Alignment.Center else Alignment.BottomCenter
    ) {
        val screenHeight = maxHeight
        Column(
            modifier = Modifier
                .applyIfNull(heightContent) {
                    onSizeChanged { with(density) { heightContent = it.height.toDp() } }
                }
                .applyIfNotNull {
                    runIfOrNull(!isContentCenter) {
                        heightContent?.let {
                            padding(bottom = screenHeight.half - it.half)
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = appTypography.h4.semiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = appTypography.h5.regular,
                color = appColor.text.subtitle,
                textAlign = TextAlign.Center
            )
        }
    }
}