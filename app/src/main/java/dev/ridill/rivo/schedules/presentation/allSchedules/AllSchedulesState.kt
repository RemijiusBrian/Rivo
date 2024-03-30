package dev.ridill.rivo.schedules.presentation.allSchedules

import dev.ridill.rivo.core.domain.util.LocaleUtil
import java.util.Currency

data class AllSchedulesState(
    val showNotificationRationale: Boolean = false,
    val currency: Currency = LocaleUtil.defaultCurrency,
    val multiSelectionModeActive: Boolean = false,
    val selectedScheduleIds: Set<Long> = emptySet(),
    val showDeleteSelectedSchedulesConfirmation: Boolean = false
)