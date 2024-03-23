package dev.ridill.rivo.schedules.presentation.allSchedules

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.LocaleUtil

data class AllSchedulesState(
    val currency: Currency = LocaleUtil.defaultCurrency,
    val multiSelectionModeActive: Boolean = false,
    val selectedScheduleIds: Set<Long> = emptySet(),
    val showDeleteSelectedSchedulesConfirmation: Boolean = false
)