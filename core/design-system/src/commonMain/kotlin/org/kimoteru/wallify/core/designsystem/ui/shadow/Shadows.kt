package org.kimoteru.wallify.core.designsystem.ui.shadow

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
fun Modifier.shadowPlus(
    color: Color,
    cornerShape: Shape = CircleShape,
    elevation: Dp = 8.dp
): Modifier = shadow(
    elevation = elevation,
    shape = cornerShape,
    ambientColor = color,
    spotColor = color
)