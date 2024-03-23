package dev.ridill.rivo.schedules.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class ScheduleRepeatMode(
    @StringRes val labelRes: Int
) {
    NO_REPEAT(R.string.transaction_repeat_mode_one_time),
    WEEKLY(R.string.transaction_repeat_mode_weekly),
    MONTHLY(R.string.transaction_repeat_mode_monthly),
    BI_MONTHLY(R.string.transaction_repeat_mode_bi_monthly),
    YEARLY(R.string.transaction_repeat_mode_yearly)
}