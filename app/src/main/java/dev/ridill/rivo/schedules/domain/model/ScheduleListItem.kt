package dev.ridill.rivo.schedules.domain.model

import androidx.compose.runtime.Composable
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDateTime

data class ScheduleListItem(
    val id: Long,
    val amount: Double,
    val note: String?,
    val nextReminderDate: LocalDateTime?,
    val lastPaidDate: LocalDateTime?
) {
    val canMarkPaid: Boolean
        get() {
            val dateNow = DateUtil.dateNow()
            return nextReminderDate?.month == dateNow.month
        }

    val amountFormatted: String
        @Composable get() = TextFormat.currencyAmount(amount)

    val nextReminderDateFormatted: String?
        get() = nextReminderDate?.format(DateUtil.Formatters.localizedDateMedium)

    val lastPaymentDateFormatted: String?
        get() = lastPaidDate?.format(DateUtil.Formatters.localizedDateMedium)
}