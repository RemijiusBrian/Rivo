package dev.ridill.mym.settings.presentation.backupSettings

import dev.ridill.mym.settings.domain.modal.BackupInterval

data class BackupSettingsState(
    val accountEmail: String? = null,
    val isAccountAdded: Boolean = false,
    val interval: BackupInterval = BackupInterval.NEVER,
    val showBackupIntervalSelection: Boolean = false,
    val isBackupRunning: Boolean = false
)