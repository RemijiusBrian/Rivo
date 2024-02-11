package dev.ridill.rivo.settings.presentation.backupEncryption

data class BackupEncryptionState(
    val hasExistingPassword: Boolean = false,
    val showPasswordInput: Boolean = false,
    val isLoading: Boolean = false
)