package dev.ridill.rivo.schedules.domain.model

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleListItem(
    val id: Long,
    val amount: Double,
    val note: String?,
    val nextReminderDate: LocalDate?,
    val lastPaymentTimestamp: LocalDateTime?,
    val canMarkPaid: Boolean
) {
    fun amountFormatted(currency: Currency): String =
        TextFormat.currency(amount, currency)

    val nextReminderDateFormatted: String?
        get() = nextReminderDate?.format(DateUtil.Formatters.localizedDateMedium)

    val lastPaymentDateFormatted: String?
        get() = lastPaymentTimestamp?.format(DateUtil.Formatters.localizedDateMedium)
}