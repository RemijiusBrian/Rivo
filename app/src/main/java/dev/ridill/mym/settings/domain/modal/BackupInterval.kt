package dev.ridill.mym.settings.domain.modal

import dev.ridill.mym.R

enum class BackupInterval(
    override val labelRes: Int,
    val daysInterval: Long
) : BaseRadioOption {
    DAILY(R.string.backup_interval_daily, 1L),
    WEEKLY(R.string.backup_interval_weekly, 7L),
    MANUAL(R.string.backup_interval_manual, -1L)
}