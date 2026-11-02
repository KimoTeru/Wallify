package org.kimoteru.wallify.core.designsystem.extensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Dp.times1_5: Dp get() = (this.value * 1.5f).dp
val Dp.times2: Dp get() = (this.value * 2f).dp
val Dp.times3: Dp get() = (this.value * 3f).dp
val Dp.times4: Dp get() = (this.value * 4f).dp

val Dp.half: Dp get() = (this.value / 2f).dp
val Dp.third: Dp get() = (this.value / 3f).dp
val Dp.quarter: Dp get() = (this.value / 4f).dp
val Dp.fifth: Dp get() = (this.value / 5f).dp

fun maxOf(a: Dp?, b: Dp?): Dp? {
    return if (a != null && b != null)
        maxOf(a.value, b.value).dp
    else null
}