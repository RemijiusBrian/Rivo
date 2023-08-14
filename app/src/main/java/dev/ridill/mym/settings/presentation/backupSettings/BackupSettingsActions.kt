package dev.ridill.mym.settings.presentation.backupSettings

import dev.ridill.mym.settings.domain.modal.BackupInterval

interface BackupSettingsActions {
    fun onBackupAccountClick()
    fun onBackupIntervalPreferenceClick()
    fun onBackupIntervalSelected(interval: BackupInterval)
    fun onBackupIntervalSelectionDismiss()
    fun onBackupNowClick()
}