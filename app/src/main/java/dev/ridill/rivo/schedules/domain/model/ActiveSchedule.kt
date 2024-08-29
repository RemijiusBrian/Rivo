package dev.ridill.rivo.schedules.domain.model

import androidx.compose.runtime.Composable
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import java.time.LocalDateTime

data class ActiveSchedule(
    val id: Long,
    val note: UiText,
    val amount: Double,
    val dueDate: LocalDateTime
) {
    val amountFormatted: String
        @Composable get() = TextFormat.currencyAmount(amount)

    val dueDateFormatted: String
        get() = "${dueDate.format(DateUtil.Formatters.EEE_ddth_commaSep)} at ${
            dueDate.format(
                DateUtil.Formatters.localizedTimeShort
            )
        }"
}