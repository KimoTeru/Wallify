package org.kimoteru.wallify.core.designsystem.ui.click

import androidx.compose.foundation.clickable
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import kotlin.time.Clock

internal object ClickDebounce {
    private var lastEventTimeMs: Long = 0
    private val now: Long
        get() = Clock.System.now().toEpochMilliseconds()

    fun onClick(click: () -> Unit) {
        if (now - lastEventTimeMs >= 300L) {
            click.invoke()
            lastEventTimeMs = now
        }
    }
}

/**
 * `clickable` that ignores a second tap within a short time (protection from double taps).
 *
 * A plain modifier chain, not `composed {}`: `composed` adds a composition with its own `remember` to every element
 * that carries it (every card of a grid) and keeps the modifier from being skipped. `interactionSource = null` lets
 * `clickable` create the source only when the element is pressed, and `ripple()` is an `IndicationNodeFactory`,
 * not a composable (Compose guide "Custom modifiers", Material ripple API).
 */
fun Modifier.debouncedClickable(
    enabled: Boolean = true,
    animateClick: Boolean = true,
    onClick: () -> Unit = {}
): Modifier = clickable(
    enabled = enabled,
    interactionSource = null,
    indication = if (animateClick && enabled) ripple() else null,
    onClick = { ClickDebounce.onClick(onClick) }
)