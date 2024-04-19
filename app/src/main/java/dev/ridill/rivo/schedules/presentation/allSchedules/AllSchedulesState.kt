package dev.ridill.rivo.schedules.presentation.allSchedules

data class AllSchedulesState(
    val showNotificationRationale: Boolean = false,
    val multiSelectionModeActive: Boolean = false,
    val selectedScheduleIds: Set<Long> = emptySet(),
    val showDeleteSelectedSchedulesConfirmation: Boolean = false
)