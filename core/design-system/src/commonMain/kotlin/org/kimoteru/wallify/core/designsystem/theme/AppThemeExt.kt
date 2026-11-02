package org.kimoteru.wallify.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

fun TextStyle.withColor(color: Color) = this.copy(color = color)

fun Color.withAlpha(alpha: Float) = this.copy(alpha = alpha)