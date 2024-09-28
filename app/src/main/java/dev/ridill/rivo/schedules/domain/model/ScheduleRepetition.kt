package dev.ridill.rivo.schedules.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class ScheduleRepetition(
    @StringRes val labelRes: Int
) {
    NO_REPEAT(R.string.schedule_repetition_no_repeat),
    WEEKLY(R.string.schedule_repetition_weekly),
    MONTHLY(R.string.schedule_repetition_monthly),
    BI_MONTHLY(R.string.schedule_repetition_bi_monthly),
    YEARLY(R.string.schedule_repetition_yearly)
}