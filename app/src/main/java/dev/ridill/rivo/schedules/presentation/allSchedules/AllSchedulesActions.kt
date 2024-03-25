package dev.ridill.rivo.schedules.presentation.allSchedules

interface AllSchedulesActions {
    fun onNotificationWarningClick()
    fun onNotificationRationaleDismiss()
    fun onNotificationRationaleAgree()
    fun onMarkSchedulePaidClick(id: Long)
    fun onScheduleLongPress(id: Long)
    fun onScheduleSelectionToggle(id: Long)
    fun onMultiSelectionModeDismiss()
    fun onDeleteSelectedSchedulesClick()
    fun onDeleteSelectedSchedulesDismiss()
    fun onDeleteSelectedSchedulesConfirm()
}