package dev.ridill.rivo.schedules.presentation.scheduleDashboard

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.schedules.domain.model.PlanListItem

interface SchedulesDashboardActions {
    fun onPlanClick(plan: PlanListItem)
    fun onNewPlanClick()
    fun onPlanInputDismiss()
    fun onPlanInputNameChange(value: String)
    fun onPlanInputColorChange(color: Color)
    fun onPlanInputConfirm()
    fun onDeleteActivePlanClick()
    fun onDeletePlanDismiss()
    fun onDeletePlanConfirm()
    fun onMarkSchedulePaidClick(id: Long)
    fun onScheduleLongPress(id: Long)
    fun onScheduleSelectionToggle(id: Long)
    fun onMultiSelectionModeDismiss()
    fun onDeleteSelectedSchedulesClick()
    fun onDeleteSelectedSchedulesDismiss()
    fun onDeleteSelectedSchedulesConfirm()
}