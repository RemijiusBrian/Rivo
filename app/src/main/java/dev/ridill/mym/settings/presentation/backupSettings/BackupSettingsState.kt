package dev.ridill.mym.settings.presentation.backupSettings

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.settings.domain.modal.BackupInterval
import java.time.LocalDateTime

data class BackupSettingsState(
    val accountEmail: String? = null,
    val isAccountAdded: Boolean = false,
    val interval: BackupInterval = BackupInterval.NEVER,
    val showBackupIntervalSelection: Boolean = false,
    val isBackupRunning: Boolean = false,
    val lastBackupDateTime: LocalDateTime? = null
) {
    val lastBackupDateFormatted: String?
        get() = lastBackupDateTime?.format(DateUtil.Formatters.localizedDateLong)

    val lastBackupTimeFormatted: String?
        get() = lastBackupDateTime?.format(DateUtil.Formatters.localizedTimeShort)
}