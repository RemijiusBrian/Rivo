package dev.ridill.mym.core.domain.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Double.Companion.Zero: Double get() = 0.0
fun Double?.orZero(): Double = this ?: Double.Zero

val Float.Companion.Zero: Float get() = 0f
val Float.Companion.One: Float get() = 1f
inline fun Float.ifNaN(value: () -> Float): Float = if (isNaN()) value() else this

val Int.Companion.Zero: Int get() = 0
fun Int?.orZero(): Int = this ?: Int.Zero

val Long.Companion.Zero: Long get() = 0L

val Dp.Companion.Zero: Dp get() = 0.dp

val String.Companion.Empty: String get() = ""
val String.Companion.WhiteSpace: String get() = " "