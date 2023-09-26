package dev.ridill.rivo.core.domain.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.nio.ByteBuffer
import java.util.UUID

val Double.Companion.Zero: Double get() = 0.0
fun Double?.orZero(): Double = this ?: Double.Zero

val Float.Companion.Zero: Float get() = 0f
val Float.Companion.One: Float get() = 1f
inline fun Float.ifNaN(value: () -> Float): Float = if (isNaN()) value() else this

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
fun String.toUUID(): UUID = UUID.nameUUIDFromBytes(this.toByteArray())