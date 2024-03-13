package dev.ridill.rivo.transactionSchedules.domain.model

import java.time.LocalDate

sealed class ScheduleListItemUiModel {
    data class DateSeparator(val date: LocalDate) : ScheduleListItemUiModel()
    data class ScheduleItem(val scheduleItem: TxScheduleListItem) : ScheduleListItemUiModel()
    data object RetiredSeparator : ScheduleListItemUiModel()
}