package dev.ridill.rivo.transactionSchedules.domain.model

import dev.ridill.rivo.core.ui.util.UiText

sealed class ScheduleListItemUiModel {
    data class ScheduleItem(val scheduleItem: TxScheduleListItem) : ScheduleListItemUiModel()
    data class TypeSeparator(val label: UiText) : ScheduleListItemUiModel()
}