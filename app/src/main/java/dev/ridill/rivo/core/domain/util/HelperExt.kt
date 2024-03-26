package dev.ridill.rivo.core.domain.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.nio.ByteBuffer
import java.util.UUID

val Double.Companion.Zero: Double get() = 0.0
fun Double?.orZero(): Double = this ?: Double.Zero
inline fun Double.ifInfinite(value: () -> Double): Double = if (this.isInfinite()) value() else this
inline fun Double.ifNaN(value: () -> Double): Double = if (this.isNaN()) value() else this

val Float.Companion.Zero: Float get() = 0f
val Float.Companion.One: Float get() = 1f
fun Float?.orZero(): Float = this ?: Float.Zero

val Int.Companion.Zero: Int get() = 0
val Int.Companion.One: Int get() = 1
fun Int?.orZero(): Int = this ?: Int.Zero
fun Int.toByteArray(): ByteArray = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
fun ByteArray.toInt(): Int = ByteBuffer.wrap(this).int

val Long.Companion.Zero: Long get() = 0L
fun Long?.orZero(): Long = this ?: Long.Zero

val Dp.Companion.Zero: Dp get() = 0.dp

val String.Companion.Empty: String get() = ""
val String.Companion.WhiteSpace: String get() = " "
val String.Companion.NewLine: String get() = "\n"
fun String.toUUID(): UUID = UUID.nameUUIDFromBytes(this.toByteArray())

fun Boolean?.orFalse(): Boolean = this ?: false
fun Boolean?.orTrue(): Boolean = this ?: true

fun <T> Set<T>.addOrRemove(element: T): Set<T> =
    if (element in this) this - element
    else this + element