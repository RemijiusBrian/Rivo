package dev.ridill.rivo.schedules.domain.model

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDate
import java.util.Currency

data class ScheduleListItem(
    val id: Long,
    val amount: Double,
    val note: String?,
    val nextReminderDate: LocalDate?,
    val lastPaidDate: LocalDate?
) {
    val canMarkPaid: Boolean
        get() {
            val dateNow = DateUtil.dateNow()
            return nextReminderDate?.month == dateNow.month
        }

    fun amountFormatted(currency: Currency): String =
        TextFormat.currency(amount, currency)

    val nextReminderDateFormatted: String?
        get() = nextReminderDate?.format(DateUtil.Formatters.localizedDateMedium)

    val lastPaymentDateFormatted: String?
        get() = lastPaidDate?.format(DateUtil.Formatters.localizedDateMedium)
}