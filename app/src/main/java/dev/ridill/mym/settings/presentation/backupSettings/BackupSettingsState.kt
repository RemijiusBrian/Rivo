package dev.ridill.mym.settings.presentation.backupSettings

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.modal.BackupInterval
import java.time.LocalDateTime

data class BackupSettingsState(
    val accountEmail: String? = null,
    val isAccountAdded: Boolean = false,
    val interval: BackupInterval = BackupInterval.MANUAL,
    val showBackupIntervalSelection: Boolean = false,
    val isBackupRunning: Boolean = false,
    val lastBackupDateTime: LocalDateTime? = null
) {
    val lastBackupDateFormatted: UiText?
        get() = lastBackupDateTime?.let { DateUtil.Formatters.prettyDateAgo(it.toLocalDate()) }

    val lastBackupTimeFormatted: String?
        get() = lastBackupDateTime?.format(DateUtil.Formatters.localizedTimeShort)
}