package org.kimoteru.wallify.core.designsystem.ui.modifier

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity

fun Modifier.applyIfNotNull(block: Modifier.() -> Modifier?): Modifier {
    return then(Modifier.block() ?: Modifier)
}

fun Modifier.applyIf(value: Boolean, block: Modifier.() -> Modifier): Modifier {
    return then(if (value) Modifier.block() else Modifier)
}

fun <T> Modifier.applyIfNull(value: T?, block: Modifier.() -> Modifier?): Modifier {
    return if (value == null) {
        then(Modifier.block() ?: Modifier)
    } else {
        this
    }
}

@Composable
fun Modifier.applyIfComposable(value: Boolean, block: @Composable Modifier.() -> Modifier): Modifier {
    return then(if (value) Modifier.block() else Modifier)
}

@Composable
fun Modifier.applyIfNotNullComposable(block: @Composable Modifier.() -> Modifier?): Modifier {
    return then(Modifier.block() ?: Modifier)
}

/** Picks exactly one modifier for [value], so a chain of checks of the same value is one call. */
@Composable
inline fun <T> Modifier.applyIf(
    value: T,
    block: @Composable Modifier.(T) -> Modifier
): Modifier = block(value)

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

fun Modifier.columnImePadding() = composed {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val keyboardHeight = WindowInsets.ime.getBottom(density)
    var prevKeyboardHeight by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = keyboardHeight) {
        if (keyboardHeight > prevKeyboardHeight) {
            scrollState.animateScrollBy(Float.MAX_VALUE)
        }
        prevKeyboardHeight = keyboardHeight
    }

    return@composed this
        .imePadding()
        .verticalScroll(scrollState)
}