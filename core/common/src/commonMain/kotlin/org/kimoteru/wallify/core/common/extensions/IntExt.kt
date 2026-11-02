package org.kimoteru.wallify.core.common.extensions

val Int.times1_5: Int get() = (this * 1.5f).toInt()
val Int.times2: Int get() = this * 2
val Int.times3: Int get() = this * 3
val Int.times4: Int get() = this * 4

val Int.half: Int get() = this / 2
val Int.quarter: Int get() = this / 4
val Int.fifth: Int get() = this / 5

val Int?.isPair: Boolean get() =  this != null && this % 2 == 0

val Int?.isMoreZero: Boolean get() = this != null && this > 0
val Int?.isMoreOne: Boolean get() = this != null && this > 1
val Int?.isZero: Boolean get() = this != null && this == 0
val Int?.isOne: Boolean get() = this != null && this == 1

val Int?.isMoreZeroValue: Int? get() = if (isMoreZero) this else null
val Int?.isMoreOneValue: Int? get() = if (isMoreOne) this else null
val Int?.isZeroValue: Int? get() = if (isZero) this else null

fun Int.zeroToNull() = if (isZero) null else this

fun Int.toNullIf(converter: (Int) -> Boolean): Int? {
    return if (converter(this)) null else this
}