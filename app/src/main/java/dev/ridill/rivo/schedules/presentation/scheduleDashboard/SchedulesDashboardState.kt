package dev.ridill.rivo.schedules.presentation.scheduleDashboard

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.LocaleUtil

data class SchedulesDashboardState(
    val currency: Currency = LocaleUtil.defaultCurrency,
    val showPlanInput: Boolean = false,
    val showDeletePlanConfirmation: Boolean = false,
    val multiSelectionModeActive: Boolean = false,
    val selectedScheduleIds: Set<Long> = emptySet(),
    val showDeleteSelectedSchedulesConfirmation: Boolean = false
)