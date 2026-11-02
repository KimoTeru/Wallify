package org.kimoteru.wallify.core.designsystem.ui.click

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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

fun Modifier.debouncedClickable(
    enabled: Boolean = true,
    animateClick: Boolean = true,
    onClick: () -> Unit = {}
): Modifier = composed {
    Modifier.clickable(
        enabled = enabled,
        onClick = { ClickDebounce.onClick(onClick) },
        interactionSource = remember { MutableInteractionSource() },
        indication = if (animateClick && enabled) ripple() else null
    )
}