package dev.ridill.rivo.schedules.domain.model

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import java.time.LocalDate

data class UpcomingSchedule(
    val id: Long,
    val note: UiText,
    val amount: Double,
    val dueDate: LocalDate
) {
    fun amountFormatted(currency: Currency): String =
        TextFormat.currency(amount, currency)

    val dueDateFormatted: String
        get() = dueDate.format(DateUtil.Formatters.EEE_ddth_commaSep)
}