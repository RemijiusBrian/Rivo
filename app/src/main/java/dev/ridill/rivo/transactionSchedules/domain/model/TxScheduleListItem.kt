package dev.ridill.rivo.transactionSchedules.domain.model

import android.icu.util.Currency
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class TxScheduleListItem(
    val id: Long,
    val amount: Double,
    val note: String?,
    val nextReminderDate: LocalDate?,
    val lastPaymentTimestamp: LocalDateTime?,
    val status: TxScheduleStatus
) {
    fun amountFormatted(currency: Currency): String =
        TextFormat.currency(amount, currency)
}