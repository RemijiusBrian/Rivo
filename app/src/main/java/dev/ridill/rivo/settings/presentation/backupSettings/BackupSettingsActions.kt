package dev.ridill.rivo.settings.presentation.backupSettings

import dev.ridill.rivo.settings.domain.modal.BackupInterval

interface BackupSettingsActions {
    fun onBackupAccountClick()
    fun onBackupIntervalPreferenceClick()
    fun onBackupIntervalSelected(interval: BackupInterval)
    fun onBackupIntervalSelectionDismiss()
    fun onBackupNowClick()
    fun onEncryptionPreferenceClick()
}