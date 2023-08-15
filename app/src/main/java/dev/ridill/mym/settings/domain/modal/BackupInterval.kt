package dev.ridill.mym.settings.domain.modal

import androidx.annotation.StringRes
import dev.ridill.mym.R

enum class BackupInterval(
    @StringRes val labelRes: Int,
    val daysInterval: Long
) {
    DAILY(R.string.backup_interval_daily, 1L),
    WEEKLY(R.string.backup_interval_weekly, 7L),
    MANUAL(R.string.backup_interval_manual, -1L)
}