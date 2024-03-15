package dev.ridill.rivo.schedules.domain.model

import dev.ridill.rivo.core.ui.util.UiText

sealed class ScheduleListItemUiModel {
    data class ScheduleItem(val scheduleItem: ScheduleListItem) : ScheduleListItemUiModel()
    data class TypeSeparator(val label: UiText) : ScheduleListItemUiModel()
}