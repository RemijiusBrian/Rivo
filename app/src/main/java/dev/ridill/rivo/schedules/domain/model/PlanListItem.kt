package dev.ridill.rivo.schedules.domain.model

import android.icu.util.Currency
import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDateTime

data class PlanListItem(
    val id: Long,
    val name: String,
    val color: Color,
    val createdTimestamp: LocalDateTime,
    val totalAmount: Double,
    val paidAmount: Double
) {
    val completionPercent: Float
        get() = (paidAmount / totalAmount).toFloat()

    fun totalAmountFormatted(currency: Currency): String =
        TextFormat.currency(totalAmount, currency)

    fun paidAmountFormatted(currency: Currency): String =
        TextFormat.currency(paidAmount, currency)
}